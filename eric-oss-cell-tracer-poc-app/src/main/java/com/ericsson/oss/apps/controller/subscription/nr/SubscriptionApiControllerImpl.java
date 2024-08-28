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
package com.ericsson.oss.apps.controller.subscription.nr;

import com.ericsson.oss.apps.api.controller.SubscriptionApi;
import com.ericsson.oss.apps.api.model.controller.SubscriptionDto;
import com.ericsson.oss.apps.model.subscription.Subscription;
import com.ericsson.oss.apps.service.SubscriptionScheduler;
import com.ericsson.oss.apps.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.function.Supplier;

@Lazy
@RequiredArgsConstructor
@RestController
public class SubscriptionApiControllerImpl implements SubscriptionApi {

    private final SubscriptionScheduler subscriptionScheduler;
    private final SubscriptionService subscriptionService;

    @Override
    public ResponseEntity<Void> blankSubscription() {
        return manageReply(subscriptionScheduler::abortScheduledSubscription);
    }

    @Override
    public ResponseEntity<SubscriptionDto> getSubscription() {
        return subscriptionService.getSubscription()
                .map(Subscription::toSubscriptionDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<Void> updateSubscription(SubscriptionDto subscription) {
        if (subscription.getStartTime() == null) {
            subscription.setStartTime(System.currentTimeMillis());
        }
        if (subscription.getEndTime() != null &&
                subscription.getEndTime() < subscription.getStartTime()) {
            return ResponseEntity.badRequest().build();
        }

        return manageReply(() -> {
            Subscription subscriptionEntity = new Subscription(subscription);
            boolean result = subscriptionScheduler.abortScheduledSubscription(subscriptionEntity);
            result &= subscriptionScheduler.scheduleSubscription(subscriptionEntity);
            return result;
        });
    }

    private ResponseEntity<Void> manageReply(Supplier<Boolean> operation) {
        if (operation.get()) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }
}
