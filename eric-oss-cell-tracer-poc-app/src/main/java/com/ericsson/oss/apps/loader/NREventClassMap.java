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
package com.ericsson.oss.apps.loader;

import com.ericsson.oss.apps.api.model.controller.TrcLevel;
import com.ericsson.oss.apps.model.NREventClass;
import com.ericsson.oss.apps.model.subscription.Subscription;
import com.ericsson.pm_event.PmEventOuterClass;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class NREventClassMap {

    private final Map<Long, NREventClass> eventIdClassMap = new ConcurrentHashMap<>();
    private final Map<TrcLevel, Set<Long>> trcLevelEidMap = new ConcurrentHashMap<>();
    private final Map<PmEventOuterClass.PmEventMessageGroup, Set<Long>> msgGroupEidMap = new ConcurrentHashMap<>();

    public Optional<NREventClass> getEventClass(long eventId) {
        return Optional.ofNullable(eventIdClassMap.get(eventId));
    }

    public List<NREventClass> getEventClasses() {
        return eventIdClassMap.values().stream().toList();
    }

    public void putEventClass(NREventClass nrEventClass) {
        eventIdClassMap.put(nrEventClass.eventId(), nrEventClass);
        mergeIdsInMap(nrEventClass.traceLevel(), nrEventClass.eventId(), trcLevelEidMap);
        mergeIdsInMap(nrEventClass.pmEventMessageGroup(), nrEventClass.eventId(), msgGroupEidMap);
    }

    private <T> void mergeIdsInMap(T eventGroup, long eventId, Map<T, Set<Long>> eventGroupMap) {
        var eventIdSet = new HashSet<>(Set.of(eventId));
        eventGroupMap.merge(eventGroup, eventIdSet,
                (v1, v2) -> {
                    v1.addAll(v2);
                    return v1;
                });
    }

    public Optional<List<Long>> trcLevelToEIds(List<Long> eId, List<TrcLevel> traceLevelList) {
        return filtersToEIds(eId, traceLevelList, Collections.emptyList());
    }

    public Optional<List<Long>> filtersToEIds(Subscription subscriptionEntity) {
        return filtersToEIds(subscriptionEntity.getEventIdList(),
                subscriptionEntity.getTrcLevelList(),
                subscriptionEntity.getEventMessageGroupList());
    }

    private Optional<List<Long>> filtersToEIds(List<Long> eId, List<TrcLevel> traceLevelList, List<PmEventOuterClass.PmEventMessageGroup> pmEventMessageGroupList) {
        // no filters
        if (eId.isEmpty() && traceLevelList.isEmpty() && pmEventMessageGroupList.isEmpty()) {
            return Optional.empty();
        }
        Set<Long> eidSet = eId.isEmpty() ? this.eventIdClassMap.keySet() : new HashSet<>(eId);

        Set<Long> trcLevelEventIdSet = getGroupEventIdSet(traceLevelList, trcLevelEidMap);
        Set<Long> msgGroupEventIdSet = getGroupEventIdSet(pmEventMessageGroupList, msgGroupEidMap);
        return Optional.of(eidSet.stream()
                .filter(trcLevelEventIdSet::contains)
                .filter(msgGroupEventIdSet::contains)
                .sorted()
                .toList());
    }

    private <T> Set<Long> getGroupEventIdSet(List<T> pmEventGroups, Map<T, Set<Long>> eventGroupMap) {
        if (pmEventGroups.isEmpty()) {
            return this.eventIdClassMap.keySet();
        } else {
            return pmEventGroups.stream()
                    .flatMap(pmEventGroup -> eventGroupMap.getOrDefault(pmEventGroup, Collections.emptySet()).stream())
                    .collect(Collectors.toSet());
        }
    }

}
