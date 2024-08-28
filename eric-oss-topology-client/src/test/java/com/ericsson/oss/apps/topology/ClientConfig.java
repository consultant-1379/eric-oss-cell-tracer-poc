package com.ericsson.oss.apps.topology; /*******************************************************************************
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Configuration
public class ClientConfig {
    @Bean
    public WebClient webClient(WebClient.Builder webClientBuilder,
                               ClientHttpConnector clientHttpConnector,
                               List<ExchangeFilterFunction> filters
    ) {
        return webClientBuilder
                .clientConnector(clientHttpConnector)
                .filters(webClientFilters -> webClientFilters.addAll(filters))
                .build();
    }
}
