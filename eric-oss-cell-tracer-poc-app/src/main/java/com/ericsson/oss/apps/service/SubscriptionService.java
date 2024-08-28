/*******************************************************************************
 * COPYRIGHT Ericsson 2023
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/

package com.ericsson.oss.apps.service;

import com.ericsson.oss.apps.loader.CmDataLoader;
import com.ericsson.oss.apps.loader.NREventClassMap;
import com.ericsson.oss.apps.model.subscription.Subscription;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.SubscriptionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final NREventClassMap nrEventClassMap;
    private final SubscriptionRepository subscriptionRepository;
    private final DccService dccService;
    private final CmDataLoader cmDataLoader;

    public static final String SUBSCRIPTION_NAME = "5g-pm-events";
    public static final String NODE_PREDICATE_NAME = "nodeName";
    public static final String EVENT_ID_PREDICATE_NAME = "eventId";

    @CacheEvict(value = {"allowedGnodebs", "allowedEventIds"}, allEntries = true)
    public boolean updateSubscription(Subscription subscriptionEntity) {
        Map<String, List<String>> predicates = getPredicates(subscriptionEntity);
        return updateSubscription("update",
                () -> subscriptionEntity.isActive() ? dccService.patchDccSubscription(SUBSCRIPTION_NAME, predicates) : dccService.blankDccSubscription(SUBSCRIPTION_NAME),
                () -> subscriptionRepository.save(subscriptionEntity)
        );
    }

    private void updateSubscription(Consumer<Subscription> updateSubscription) {
        Subscription subscription = subscriptionRepository.findById(SUBSCRIPTION_NAME)
                .orElse(new Subscription());
        updateSubscription.accept(subscription);
        subscriptionRepository.save(subscription);
    }

    @NotNull
    private Map<String, List<String>> getPredicates(Subscription subscriptionEntity) {
        Optional<List<Long>> eventIdSet = nrEventClassMap.filtersToEIds(subscriptionEntity);
        Map<String, List<String>> predicates = new HashMap<>();
        predicates.put(NODE_PREDICATE_NAME, subscriptionEntity.getGNodebMoIdList().stream().map(ManagedObjectId::toString).toList());
        eventIdSet.map(eidList -> eidList.stream().map(id -> Long.toString(id)).toList())
                .ifPresent(eidList -> predicates.put(EVENT_ID_PREDICATE_NAME, eidList));
        return Collections.unmodifiableMap(predicates);
    }

    public Optional<Subscription> getSubscription() {
        return subscriptionRepository.findById(SUBSCRIPTION_NAME);
    }

    @Transactional
    @CacheEvict(value = {"allowedGnodebs", "allowedEventIds"}, allEntries = true)
    public boolean blankSubscription() {
        return blankSubscription(sub -> sub.setActive(false));
    }

    @Transactional
    @CacheEvict(value = {"allowedGnodebs", "allowedEventIds"}, allEntries = true)
    public boolean blankSubscription(Consumer<Subscription> updateSubscription) {
        return updateSubscription(
                "blank",
                () -> dccService.blankDccSubscription(SUBSCRIPTION_NAME),
                () -> updateSubscription(updateSubscription)
        );
    }

    private synchronized boolean updateSubscription(String op, Supplier<Boolean> dccOperation, Runnable subRepoOperation) {
        try {
            boolean isSubscriptionUpdated = dccOperation.get();
            if (isSubscriptionUpdated) {
                refreshCmInfo();
                log.debug("Subscription {} in dcc.", op);
                subRepoOperation.run();
                log.debug("Subscription persisted.");
                return true;
            }
            log.error("Cannot {} subscription because of problems with upstream communication (dcc).", dccOperation);
            return false;
        } catch (RuntimeException e) {
            log.error("Cannot {} subscription because of ", dccOperation, e);
            return false;
        }
    }

    @Cacheable(value = "allowedGnodebs")
    public boolean isGnodebAllowed(ManagedObjectId gNodeB) {
        return isElementAllowed(gNodeB, (node, subscriptionEntity) -> subscriptionEntity.getGNodebMoIdList().contains(node));
    }

    @Cacheable(value = "allowedEventIds")
    public boolean isEventIdAllowed(long eventId) {
        return isElementAllowed(eventId, (eid, subscriptionEntity) -> nrEventClassMap.filtersToEIds(subscriptionEntity).map(eventList -> eventList.contains(eid)).orElse(true));
    }

    private <T> boolean isElementAllowed(T element, BiFunction<T, Subscription, Boolean> isAllowedFunction) {
        return subscriptionRepository.findById(SUBSCRIPTION_NAME)
                .map(subscriptionEntity -> subscriptionEntity.isActive() && isAllowedFunction.apply(element, subscriptionEntity))
                .orElse(false);
    }

    @Scheduled(initialDelay = 0, fixedRateString = "${cm-extract.refreshRate}")
    void refreshCmInfo() {
        cmDataLoader.extractCmInfo(getAllowedGnodebs());
    }

    @NotNull
    private List<ManagedObjectId> getAllowedGnodebs() {
        return subscriptionRepository.findById(SUBSCRIPTION_NAME).map(Subscription::getGNodebMoIdList).orElse(List.of());
    }

}
