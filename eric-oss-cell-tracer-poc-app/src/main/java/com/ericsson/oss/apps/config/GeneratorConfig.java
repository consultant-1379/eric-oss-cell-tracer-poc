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

import com.ericsson.oss.apps.events.NREventMethodCache;
import com.ericsson.oss.apps.generator.RandomEventFactory;
import com.ericsson.oss.apps.generator.RandomEventGenerator;
import com.ericsson.oss.apps.loader.AllowedNodeMap;
import com.ericsson.oss.apps.loader.NREventClassMap;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = {"app.generator.events.enabled"}, havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class GeneratorConfig {
    @Value("${app.generator.events.seed:42}")
    private int seed;
    @Bean
    public RandomEventFactory randomEventFactory(NREventClassMap eventClassMap,
                                                 AllowedNodeMap allowedNodeMap,
                                                 NREventMethodCache nrEventMethodCache) {
        return new RandomEventFactory(eventClassMap, allowedNodeMap, nrEventMethodCache, seed);
    }

    @Bean
    public RandomEventGenerator randomEventGenerator(RandomEventFactory randomEventFactory) {
        return new RandomEventGenerator(randomEventFactory, seed);
    }

}
