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
package com.ericsson.oss.apps.config;

import com.ericsson.oss.apps.service.BlankSubscriptionJob;
import com.ericsson.oss.apps.service.SubscriptionScheduler;
import com.ericsson.oss.apps.service.UpdateSubscriptionJob;
import org.quartz.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@ConditionalOnProperty(value = "app.scheduling.enabled", havingValue = "true", matchIfMissing = true)
@Configuration
@EnableScheduling
@Lazy
public class SchedulingConfig {

    public static final String SUBSCRIPTION_TRIGGER_GROUP = "subscription-triggers";
    public static final String SUBSCRIPTION_JOB_GROUP = "subscription-jobs";
    public static final JobKey UPDATE_SUBSCRIPTION_JOB_KEY = new JobKey("update", SUBSCRIPTION_JOB_GROUP);
    public static final JobKey BLANK_SUBSCRIPTION_JOB_KEY = new JobKey("blank", SUBSCRIPTION_JOB_GROUP);

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(
            DataSource dataSource,
            QuartzProperties quartzProperties,
            PlatformTransactionManager platformTransactionManager
    ) {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(new SpringBeanJobFactory());
        factory.setDataSource(dataSource);

        Properties properties = new Properties();
        properties.putAll(quartzProperties.getProperties());

        factory.setQuartzProperties(properties);
        factory.setAutoStartup(quartzProperties.isAutoStartup());
        factory.setOverwriteExistingJobs(quartzProperties.isOverwriteExistingJobs());
        factory.setWaitForJobsToCompleteOnShutdown(quartzProperties.isWaitForJobsToCompleteOnShutdown());
        factory.setTransactionManager(platformTransactionManager);
        return factory;
    }

    @Bean
    public SubscriptionScheduler subscriptionScheduler(Scheduler scheduler) {
        return new SubscriptionScheduler(
                scheduler,updateSubscriptionJobDetail(), blankSubscriptionJobDetail()
        );
    }

    private static JobDetail updateSubscriptionJobDetail() {
        return JobBuilder.newJob(UpdateSubscriptionJob.class)
                .withIdentity(UPDATE_SUBSCRIPTION_JOB_KEY)
                .withDescription("Job for updating subscription list")
                .storeDurably(true)
                .build();
    }

    private static JobDetail blankSubscriptionJobDetail() {
        return JobBuilder.newJob(BlankSubscriptionJob.class)
                .withIdentity(BLANK_SUBSCRIPTION_JOB_KEY)
                .withDescription("Job for blanking subscription list")
                .storeDurably(true)
                .build();
    }
}
