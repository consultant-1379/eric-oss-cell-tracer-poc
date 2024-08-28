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
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainerProvider;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.ericsson.oss.apps.model.subscription.SubscriptionTest.FDN;
import static com.ericsson.oss.apps.model.subscription.SubscriptionTest.getSubscriptionEntity;
import static org.mockito.ArgumentMatchers.any;
import static org.testcontainers.shaded.org.awaitility.Awaitility.waitAtMost;

@Testcontainers
@ActiveProfiles("postgres")
@SpringBootTest(properties = {
        "logging.level.com.ericsson.oss.apps.service=DEBUG",
        "app.scheduling.enabled=true",
})
class ScheduleSubscriptionTest {

    @Container
    private static final JdbcDatabaseContainer postgreSQL = new PostgreSQLContainerProvider()
            .newInstance();

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQL::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQL::getPassword);
        registry.add("spring.datasource.username", postgreSQL::getUsername);
    }

    private static Subscription getSubscription() {
        Subscription subscription = getSubscriptionEntity(FDN);
        subscription.setEndTime(subscription.getStartTime() + 5000);
        return subscription;
    }

    @MockBean
    private SubscriptionService subscriptionService;

    @Nested
    class SchedulerTest {

        private static final String UPGRADE_MSG = "update";
        private static final String ABORT_MSG = "abort";
        private static final ConcurrentLinkedDeque<String> STATES = new ConcurrentLinkedDeque<>();

        private synchronized boolean addDelayedState(String state) {
            STATES.add(state);
            return true;
        }

        @Autowired
        private Scheduler scheduler;
        @Autowired
        private SubscriptionScheduler subscriptionScheduler;

        @BeforeEach
        void setup() throws SchedulerException {
            Mockito.when(subscriptionService.blankSubscription(any())).then(i -> addDelayedState(ABORT_MSG));
            Mockito.when(subscriptionService.updateSubscription(any(Subscription.class))).then(i -> addDelayedState(UPGRADE_MSG));

            scheduler.start();
        }

        @Nested
        @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        class PersistenceTest {
            @Order(1)
            @Test
            void testJobScheduling() {
                STATES.clear();
                Subscription subscription = getSubscription();

                subscriptionScheduler.scheduleSubscription(subscription);

                Mockito.verify(subscriptionService, Mockito.times(0)).blankSubscription(any());
                Mockito.verify(subscriptionService, Mockito.times(0)).updateSubscription(any(Subscription.class));
            }

            @Order(2)
            @Test
            void testJobPersistence() {
                waitAtMost(10, TimeUnit.SECONDS)
                        .untilAsserted(() -> {
                            Mockito.verify(subscriptionService, Mockito.times(1)).updateSubscription(any(Subscription.class));
                            Mockito.verify(subscriptionService, Mockito.times(1)).blankSubscription(any());
                            Assertions.assertLinesMatch(List.of(UPGRADE_MSG, ABORT_MSG), new LinkedList<>(STATES));
                        });
            }
        }

        @Test
        void testJobAbortAfterUpdate() {
            STATES.clear();
            Subscription subscription = getSubscription();

            subscriptionScheduler.scheduleSubscription(subscription);
            waitAtMost(15, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        Mockito.verify(subscriptionService, Mockito.times(1)).updateSubscription(any(Subscription.class));
                        Assertions.assertTrue(STATES.contains(UPGRADE_MSG));
                    });

            subscriptionScheduler.abortScheduledSubscription();

            waitAtMost(10, TimeUnit.SECONDS)
                    .untilAsserted(() -> {
                        Mockito.verify(subscriptionService, Mockito.times(1)).blankSubscription(any());
                        Assertions.assertEquals(ABORT_MSG, STATES.getLast());
                    });
        }
    }

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SubscriptionJobTest {

        private final Subscription originalSubscription = getSubscription();
        private final Subscription currentSubscription = new Subscription();

        @Mock
        private Scheduler scheduler;
        @Mock
        private JobExecutionContext jobExecutionContext;
        @Mock
        private Trigger trigger;
        @Mock
        private TriggerKey triggerKey;

        @BeforeEach
        void setup() {
            Mockito.when(jobExecutionContext.getScheduler()).thenReturn(scheduler);
            Mockito.when(jobExecutionContext.getTrigger()).thenReturn(trigger);
            Mockito.when(trigger.getKey()).thenReturn(triggerKey);
        }

        @Order(1)
        @Test
        void testUpdateJob() throws SchedulerException {
            UpdateSubscriptionJob updateSubscriptionJob = new UpdateSubscriptionJob(subscriptionService, scheduler);
            updateSubscriptionJob.setSubscriptionEntity(originalSubscription);
            updateSubscriptionJob.setNextTrigger(trigger);

            updateSubscriptionJob.execute(jobExecutionContext);

            Mockito.verify(subscriptionService, Mockito.times(1)).updateSubscription(originalSubscription);
            Mockito.verify(scheduler, Mockito.times(1)).scheduleJob(Mockito.any());
        }

        @Order(2)
        @Test
        void testBlankJob() throws JobExecutionException {
            Mockito.when(subscriptionService.blankSubscription(Mockito.any())).then(this::callConsumer);
            BlankSubscriptionJob blankSubscriptionJob = new BlankSubscriptionJob(subscriptionService);
            blankSubscriptionJob.setUpComingSubscriptionEntity(originalSubscription);

            blankSubscriptionJob.execute(jobExecutionContext);

            Mockito.verify(subscriptionService, Mockito.times(1)).blankSubscription(Mockito.any());
            Assertions.assertEquals(currentSubscription.toSubscriptionDto(), originalSubscription.toSubscriptionDto());
            Assertions.assertFalse(originalSubscription.isActive());
        }

        private Void callConsumer(InvocationOnMock invocationOnMock) {
            Consumer<Subscription> argument = invocationOnMock.getArgument(0);
            argument.accept(currentSubscription);
            return null;
        }
    }
}
