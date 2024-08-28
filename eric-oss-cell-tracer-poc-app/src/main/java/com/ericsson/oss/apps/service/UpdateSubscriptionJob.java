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
import org.springframework.scheduling.quartz.QuartzJobBean;

@Slf4j
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class UpdateSubscriptionJob extends QuartzJobBean implements InterruptableJob {

    private final SubscriptionService subscriptionService;
    private final Scheduler scheduler;
    @Setter private Subscription subscriptionEntity;
    @Setter private Trigger nextTrigger;
    private boolean isInterrupted = false;
    private TriggerKey triggerKey;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            triggerKey = context.getTrigger().getKey();
            log.debug("Scheduled subscription update starting, trigger: {}", triggerKey);
            subscriptionService.updateSubscription(subscriptionEntity);

            if (!(nextTrigger == null || isInterrupted)) {
                log.debug("Schedule subscription blanking job, trigger: {}", nextTrigger);
                scheduler.scheduleJob(nextTrigger);
            }
            log.debug("Scheduled subscription update finishing, trigger: {}", triggerKey);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        log.debug("Interrupt scheduled subscription, trigger: {}", triggerKey);
        isInterrupted = true;
    }
}
