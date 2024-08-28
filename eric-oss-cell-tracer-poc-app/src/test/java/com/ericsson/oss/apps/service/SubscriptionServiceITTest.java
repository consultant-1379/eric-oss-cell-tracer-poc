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

import com.ericsson.oss.apps.loader.NREventClassMap;
import com.ericsson.oss.apps.model.subscription.Subscription;
import com.ericsson.oss.apps.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static com.ericsson.oss.apps.service.SubscriptionService.SUBSCRIPTION_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * IT test to verify the caching behagit add vior
 */
@SpringBootTest
class SubscriptionServiceITTest {

    @SpyBean
    SubscriptionService subscriptionService;

    @MockBean
    NREventClassMap nrEventClassMap;

    @MockBean
    SubscriptionRepository subscriptionRepository;

    @Test
    void testCache() {
        Subscription subscriptionEntity = new Subscription();
        subscriptionEntity.setActive(true);

        when(subscriptionRepository.findById(SUBSCRIPTION_NAME)).thenReturn(Optional.of(subscriptionEntity));
        when(nrEventClassMap.filtersToEIds(subscriptionEntity)).thenReturn(Optional.of(List.of(42L)));

        runAndVerifyTimesCalled(2, 1, true);
        subscriptionService.updateSubscription(subscriptionEntity);
        runAndVerifyTimesCalled(2, 2, true);
        subscriptionService.blankSubscription();
        runAndVerifyTimesCalled(1, 3, false);
    }

    private void runAndVerifyTimesCalled(int ncalls, int wantedNumberOfInvocations, boolean result) {
        IntStream.range(0, ncalls).forEach(i -> assertEquals(result, subscriptionService.isEventIdAllowed(42L)));
        verify(subscriptionService, times(wantedNumberOfInvocations)).isEventIdAllowed(42L);
    }


}