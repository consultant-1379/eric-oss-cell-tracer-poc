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


import com.ericsson.oss.apps.api.model.controller.SubscriptionDto;
import com.ericsson.oss.apps.model.subscription.Subscription;
import com.ericsson.oss.apps.service.SubscriptionScheduler;
import com.ericsson.oss.apps.service.SubscriptionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Date;
import java.util.Optional;

import static com.ericsson.oss.apps.model.subscription.SubscriptionTest.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(SubscriptionApiControllerImpl.class)
class SubscriptionApiControllerImplTest {

    public static final String SUBSCRIPTION_DTO_JSON = """
            {"trcLevels":["UE"],"nwFunctions":["CUUP"],"eId":[1,2,3],"GNodebNames":["node1","node2"]}""";
    private static final String[] FDNS = new String[]{"node1", "node2"};

    @MockBean
    private SubscriptionService subscriptionService;
    @MockBean
    private SubscriptionScheduler subscriptionScheduler;
    @Autowired
    private SubscriptionApiControllerImpl subscriptionApiController;

    @MockBean
    private WebClient webClient;
    @Captor
    ArgumentCaptor<Subscription> entityCaptor;


    @Autowired
    private MockMvc mvc;

    @ParameterizedTest
    @CsvSource(value = {"true", "false"})
    void blankSubscription(boolean operationResult) throws Exception {
        when(subscriptionScheduler.abortScheduledSubscription()).thenReturn(operationResult);
        mvc.perform(delete("/v1/event/nr/subscription"))
                .andExpect(operationResult ? status().isOk() : status().is5xxServerError());
        verify(subscriptionScheduler, times(1)).abortScheduledSubscription();
    }


    @Test
    void getSubscription_ReturnsOk() throws Exception {
        Subscription subscription = getSubscriptionEntity(FDNS);
        when(subscriptionService.getSubscription()).thenReturn(Optional.of(subscription));
        mvc.perform(get("/v1/event/nr/subscription"))
                .andExpect(status().isOk())
                .andExpect(content().json(SUBSCRIPTION_DTO_JSON));
        verify(subscriptionService, times(1)).getSubscription();
    }


    @Test
    void getSubscription_ReturnsNotFound() throws Exception {
        when(subscriptionService.getSubscription()).thenReturn(Optional.empty());
        mvc.perform(get("/v1/event/nr/subscription"))
                .andExpect(status().isNotFound());
        verify(subscriptionService, times(1)).getSubscription();
    }

    @ParameterizedTest
    @CsvSource(value = {"true", "false"})
    void updateSubscription(boolean operationResult) throws Exception {
        when(subscriptionScheduler.abortScheduledSubscription(any(Subscription.class))).thenReturn(operationResult);
        when(subscriptionScheduler.scheduleSubscription(any(Subscription.class))).thenReturn(operationResult);

        mvc.perform(put("/v1/event/nr/subscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(SUBSCRIPTION_DTO_JSON))
                .andExpect(operationResult ? status().isOk() : status().is5xxServerError());
        verify(subscriptionScheduler, times(1)).scheduleSubscription(entityCaptor.capture());
        Subscription subscription = entityCaptor.getValue();
        Assertions.assertEquals(getSubscriptionDto(subscription.getStartTime(), FDNS), subscription.toSubscriptionDto());
    }

    @Test
    void updateSubscriptionBadRequest() {
        SubscriptionDto subscriptionDto = getSubscriptionDto(FDN);
        subscriptionDto.setStartTime(new Date().getTime());
        subscriptionDto.setEndTime(subscriptionDto.getStartTime() - 1000);
        ResponseEntity<Void> response = subscriptionApiController.updateSubscription(subscriptionDto);

        Assertions.assertEquals(HttpStatusCode.valueOf(400), response.getStatusCode());
    }
}