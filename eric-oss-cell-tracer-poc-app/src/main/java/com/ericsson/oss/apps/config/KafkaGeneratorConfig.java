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

import com.ericsson.oss.apps.generator.RandomEventGenerator;
import com.ericsson.oss.apps.kafka.producer.RandomEventDataSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@ConditionalOnProperty(value = {"app.generator.events.enabled", "rapp-sdk.kafka.enabled"}, havingValue = "true", matchIfMissing = true)
public class KafkaGeneratorConfig {

    @Value("${rapp-sdk.kafka.sender.events.nrtopic}")
    private String topic;

    @Bean
    public RandomEventDataSender randomEventDataSender(
            KafkaTemplate<String, byte[]> nrEventTemplate,
            RandomEventGenerator generator) {
        return new RandomEventDataSender(nrEventTemplate, generator, topic);
    }
}
