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

import com.ericsson.oss.apps.dcc.DccClient;
import com.ericsson.oss.apps.dcc.model.IDS;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j
public class DccServiceImpl implements DccService {

    final DccClient dccClient;
    final ObjectMapper mapper;

    @Override
    @EventListener(ApplicationReadyEvent.class)
    @Retryable(backoff = @Backoff(delay = 1000))
    public void createIds() {
        try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("ids/ids.json")) {
            IDS ids = mapper.readValue(inputStream, IDS.class);
            boolean result = dccClient.createIds(ids.getVersion(), ids.getSubscriptions());
            if (result) {
                log.info("IDS creation successful");
            }
            log.warn("IDS creation failed");
        } catch (IOException e) {
            log.error("Cannot create IDS, cannot read IDS specs", e);
        }
    }

    @Override
    @PreDestroy
    @Retryable(backoff = @Backoff(delay = 1000))
    public void deleteIds() {
        boolean result = dccClient.deleteIds();
        if (result) {
            log.info("IDS deletion successful");
        }
        log.warn("IDS deletion failed");
    }

    @Override
    @Retryable(backoff = @Backoff(delay = 1000))
    public boolean patchDccSubscription(String name, Map<String, List<String>> predicates) {
        return dccClient.patchIdsSubscription(name, predicates);
    }

    @Override
    @Retryable(backoff = @Backoff(delay = 1000))
    public boolean blankDccSubscription(String name) {
        return dccClient.blankIdsSubscription(name);
    }


}
