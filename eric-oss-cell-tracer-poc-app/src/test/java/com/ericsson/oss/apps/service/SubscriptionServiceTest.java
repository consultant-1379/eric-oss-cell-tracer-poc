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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.ericsson.oss.apps.model.subscription.SubscriptionTest.*;
import static com.ericsson.oss.apps.service.SubscriptionService.SUBSCRIPTION_NAME;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @InjectMocks
    SubscriptionService subscriptionService;
    @Mock
    SubscriptionRepository subscriptionRepository;
    @Mock
    DccService dccService;

    @Mock
    NREventClassMap nrEventClassMap;
    @Mock
    CmDataLoader cmDataLoader;
    @Captor
    ArgumentCaptor<Subscription> entityCaptor;

    @ParameterizedTest
    @CsvSource(
            {"true", "false"}
    )
    void updateSubscription(boolean dccResult) {
        updateSubscription(dccResult, () -> when(dccService.patchDccSubscription(SUBSCRIPTION_NAME, getPredicates())).thenReturn(dccResult), true);
    }

    @ParameterizedTest
    @CsvSource(
            {"true", "false"}
    )
    void updateDeactivateSubscription(boolean dccResult) {
        updateSubscription(dccResult, () -> when(dccService.blankDccSubscription(SUBSCRIPTION_NAME)).thenReturn(dccResult), false);
    }

    @Test
    void updateSubscriptionException() {
        updateSubscription(false, () -> when(dccService.patchDccSubscription(SUBSCRIPTION_NAME, getPredicates())).thenThrow(new RuntimeException()), true);
    }

    private static Map<String, List<String>> getPredicates() {
        return Map.of(
                SubscriptionService.NODE_PREDICATE_NAME, List.of(FDN),
                SubscriptionService.EVENT_ID_PREDICATE_NAME, List.of("1", "2")
        );
    }

    void updateSubscription(boolean dccResult, Runnable dccMockReply, boolean isActive) {
        Subscription subscription = getSubscriptionEntity(FDN);
        subscription.setActive(isActive);

        when(nrEventClassMap.filtersToEIds(any(Subscription.class))).thenReturn(Optional.of(List.of(1L, 2L)));
        dccMockReply.run();
        assertEquals(dccResult, subscriptionService.updateSubscription(subscription));

        verify(nrEventClassMap, times(1)).filtersToEIds(entityCaptor.capture());
        verify(subscriptionRepository, times(dccResult ? 1 : 0)).save(entityCaptor.getValue());
        assertEquals(isActive, entityCaptor.getValue().isActive());

        if (dccResult)
        {
            verify(cmDataLoader, times(1)).extractCmInfo(List.of());
        }
    }


    @Test
    void getSubscription() {
        Subscription subscriptionEntity = new Subscription();
        when(subscriptionRepository.findById(SUBSCRIPTION_NAME)).thenReturn(Optional.of(subscriptionEntity));
        assertEquals(Optional.of(subscriptionEntity), subscriptionService.getSubscription());
    }

    @ParameterizedTest
    @CsvSource(
            {"true", "false"}
    )
    void blankSubscription(boolean dccResult) {
        Subscription subscriptionEntity = new Subscription();
        subscriptionEntity.setGNodebMoIdList(List.of(ManagedObjectId.of(FDN)));
        subscriptionEntity.setActive(true);
        when(dccService.blankDccSubscription(SUBSCRIPTION_NAME)).thenReturn(dccResult);
        if (dccResult) {
            when(subscriptionRepository.findById(SUBSCRIPTION_NAME)).thenReturn(Optional.of(subscriptionEntity));
        }
        assertEquals(dccResult, subscriptionService.blankSubscription());
        assertNotEquals(dccResult , subscriptionEntity.isActive());
        verify(subscriptionRepository, times(dccResult ? 1 : 0)).save(subscriptionEntity);
        if (dccResult)
        {
            verify(cmDataLoader, times(1)).extractCmInfo(List.of(ManagedObjectId.of(FDN)));
        }
    }

    @Test
    void blankSubscriptionException() {
        when(dccService.blankDccSubscription(SUBSCRIPTION_NAME)).thenThrow(new RuntimeException());
        assertFalse(subscriptionService.blankSubscription());
    }

    private static Stream<Arguments> isGnodebAllowedArgs() {
        return Stream.of(
                Arguments.of(null, false, false, false),
                Arguments.of(new Subscription(), false, false, false),
                Arguments.of(new Subscription(), true, false, false),
                Arguments.of(new Subscription(), false, true, false)
        );
    }

    @ParameterizedTest
    @MethodSource("isGnodebAllowedArgs")
    void isGnodebAllowed(Subscription sub, boolean isActive, boolean isInList, boolean result) {
        Optional<Subscription> repoResult = Optional.ofNullable(sub);
        repoResult.ifPresent(subcription -> {
            subcription.setActive(isActive);
            subcription.setGNodebMoIdList(isInList ? List.of(ManagedObjectId.of(FDN)) : List.of());
        });
        when(subscriptionRepository.findById(SUBSCRIPTION_NAME)).thenReturn(repoResult);
        assertEquals(result, subscriptionService.isGnodebAllowed(ManagedObjectId.of(FDN)));
    }

    @ParameterizedTest
    @MethodSource("isGnodebAllowedArgs")
    void isEventIdAllowed(Subscription sub, boolean isActive, boolean isInList, boolean result) {
        Optional<Subscription> repoResult = Optional.ofNullable(sub);
        repoResult.ifPresent(subscription -> {
            subscription.setActive(isActive);
            if (isActive) {
                when(nrEventClassMap.filtersToEIds(subscription)).thenReturn(isInList ? Optional.of(List.of(42L)) : Optional.of(List.of()));
            }
        });
        when(subscriptionRepository.findById(SUBSCRIPTION_NAME)).thenReturn(repoResult);
        assertEquals(result, subscriptionService.isEventIdAllowed(42));
    }
}