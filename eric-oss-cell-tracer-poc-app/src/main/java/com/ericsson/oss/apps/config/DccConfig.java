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

import com.ericsson.oss.apps.dcc.DccClient;
import com.ericsson.oss.apps.service.DccService;
import com.ericsson.oss.apps.service.DccServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DccConfig {

    @Bean
    @ConditionalOnProperty(prefix = "rapp-sdk.dcc", name = "enabled", havingValue = "true")
    public DccService dccService(DccClient dccClient, ObjectMapper mapper) {
        return new DccServiceImpl(dccClient, mapper);
    }

    @Bean
    @ConditionalOnMissingBean
    public DccService dccDefaultService() {
        return new DccService() {
        };
    }

}
