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
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.Date;
import java.util.UUID;

import static com.ericsson.oss.apps.config.SchedulingConfig.*;
import static org.quartz.TriggerBuilder.newTrigger;

@Slf4j
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final Scheduler scheduler;
    private final JobDetail updateSubscriptionJobDetail;
    private final JobDetail blankSubscriptionJobDetail;

    @PostConstruct
    @Transactional
    void registerJobs() throws SchedulerException {
        if (!scheduler.checkExists(updateSubscriptionJobDetail.getKey())) {
            scheduler.addJob(updateSubscriptionJobDetail, true);
        }

        if (!scheduler.checkExists(blankSubscriptionJobDetail.getKey())) {
            scheduler.addJob(blankSubscriptionJobDetail, true);
        }
    }

    @Transactional
    public boolean scheduleSubscription(Subscription subscription) {
        try {
            Trigger subscriptionJobTrigger = convertToTrigger(subscription);
            scheduler.scheduleJob(subscriptionJobTrigger);
            log.info("Given subscription got scheduled: {}, trigger: {}", subscription, subscriptionJobTrigger.getKey());
            return true;
        } catch (SchedulerException e) {
            log.error("Couldn't schedule given subscription: {}", subscription, e);
            return false;
        }
    }

    private Trigger convertToTrigger(Subscription subscription) {
        JobDataMap jobData = new JobDataMap();
        jobData.put("subscriptionEntity", subscription);

        if (subscription.getEndTime() != null) {
            Trigger blankJobTrigger = buildTrigger(blankSubscriptionJobDetail, new Date(subscription.getEndTime()));
            jobData.put("nextTrigger", blankJobTrigger);
        }

        return buildTrigger(updateSubscriptionJobDetail, new Date(subscription.getStartTime()), jobData);
    }

    @Transactional
    public boolean abortScheduledSubscription() {
        return abortScheduledSubscription(new JobDataMap());
    }

    @Transactional
    public boolean abortScheduledSubscription(Subscription upComingSubscription) {
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("upComingSubscriptionEntity", upComingSubscription);
        return abortScheduledSubscription(jobDataMap);
    }

    private synchronized boolean abortScheduledSubscription(JobDataMap jobDataMap) {
        try {
            log.debug("Aborting scheduled subscriptions");
            scheduler.pauseJobs(GroupMatcher.groupEquals(SUBSCRIPTION_JOB_GROUP));
            if (scheduler.interrupt(UPDATE_SUBSCRIPTION_JOB_KEY)) {
                log.debug(UPDATE_SUBSCRIPTION_JOB_KEY + " got interrupted");
            }
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.groupEquals(SUBSCRIPTION_TRIGGER_GROUP))) {
                scheduler.unscheduleJob(triggerKey);
                log.debug("Un-scheduling subscription, trigger: {}", triggerKey);
            }
            scheduler.scheduleJob(buildTrigger(blankSubscriptionJobDetail, new Date(), jobDataMap));
            scheduler.resumeJobs(GroupMatcher.groupEquals(SUBSCRIPTION_JOB_GROUP));
            return true;
        } catch (SchedulerException  e) {
            log.error("Error occurred while checking previous scheduled job", e);
            return false;
        }
    }

    private Trigger buildTrigger(JobDetail jobDetail, Date date) {
        return buildTrigger(jobDetail, date, new JobDataMap());
    }

    private Trigger buildTrigger(JobDetail jobDetail, Date date, JobDataMap jobDataMap) {
        return newTrigger()
                .withIdentity(UUID.randomUUID().toString(), SUBSCRIPTION_TRIGGER_GROUP)
                .forJob(jobDetail)
                .usingJobData(jobDataMap)
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .startAt(date)
                .build();
    }
}
