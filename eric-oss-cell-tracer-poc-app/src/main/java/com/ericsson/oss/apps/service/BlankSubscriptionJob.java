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

import com.ericsson.oss.apps.model.subscription.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class BlankSubscriptionJob extends QuartzJobBean {

    private final SubscriptionService subscriptionService;
    @Setter private Subscription upComingSubscriptionEntity;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            TriggerKey triggerKey = context.getTrigger().getKey();
            log.debug("Scheduled subscription blanking starting, trigger: {}", triggerKey);
            subscriptionService.blankSubscription(this::updateSubscriptionState);

            log.debug("Scheduled subscription blanking finishing, trigger: {}", triggerKey);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    private void updateSubscriptionState(Subscription subscription) {
        if (upComingSubscriptionEntity != null) {
            BeanUtils.copyProperties(upComingSubscriptionEntity, subscription);
        }
        subscription.setActive(false);
    }
}