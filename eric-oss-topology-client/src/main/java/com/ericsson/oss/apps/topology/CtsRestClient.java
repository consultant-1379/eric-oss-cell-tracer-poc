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
package com.ericsson.oss.apps.topology;

import com.ericsson.oss.apps.topology.model.TopologyObject;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

@RequiredArgsConstructor
public class CtsRestClient {

    private final WebClient webClient;
    private final String basePath;

    public Flux<TopologyObject> fetchNodes() {
        String baseUrl = String.format("%s/ctw/gnbdu", basePath);
        Map<String, String> parameters = getParameters(0L);

        String uri = extendUriTemplate(baseUrl, parameters);

        return fetchAllTopologyObjects(uri, parameters);
    }

    private static String extendUriTemplate(String baseUrl, Map<String, String> parameters) {
        StringBuilder builder = new StringBuilder();
        parameters.forEach((k, v) -> {
            if (!builder.isEmpty()) {
                builder.append("&");
            }
            builder.append(k).append("={")
                    .append(k).append("}");
        });
        return !builder.isEmpty() ? (baseUrl + "?" + builder) : baseUrl;
    }

    private Flux<TopologyObject> fetchAllTopologyObjects(String uri, Map<String, String> parameters) {
        return fetchTopologyObjects(uri, parameters)
                .collectList()
                .flatMapMany(list -> {
                    if (!list.isEmpty()) {
                        TopologyObject last = list.get(list.size() - 1);
                        return Flux.concat(Flux.fromIterable(list), fetchAllTopologyObjects(uri, getParameters(last.getId())));
                    }
                    return Flux.fromIterable(list);
                });
    }

    private static Map<String, String> getParameters(Long id) {
        return Map.of(
                "fs", "attrs",
                "sort", "objectInstId",
                "criteria", String.format("(objectInstId > %dL)", id)
        );
    }

    private Flux<TopologyObject> fetchTopologyObjects(String uri, Map<String, ?> parameters) {
        return webClient.get()
                .uri(uri, parameters)
                .retrieve()
                .bodyToFlux(TopologyObject.class);
    }
}
