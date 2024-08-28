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

import com.ericsson.oss.apps.events.EventProcessor;
import com.ericsson.oss.apps.model.entities.NrEvent;
import com.ericsson.oss.apps.service.PmEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Bean
    public EventProcessor<NrEvent> eventPersistProcessor(PmEventService pmEventService) {
        return pmEventService::batchInsertEvents;
    }
}
