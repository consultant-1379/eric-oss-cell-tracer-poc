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

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "rapp-sdk.kafka", value = "enabled", havingValue = "true", matchIfMissing = true)
public class KafkaProducerConfig {

    @Bean
    public KafkaTemplate<String, byte[]> getNrEventTemplate(
            @Value("${rapp-sdk.kafka.bootstrap-servers}") String bootStrapServers,
            ProducerFactory<String, byte[]> pf) {
        return new KafkaTemplate<>(pf, Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class,
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers));
    }

}
