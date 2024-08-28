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
import com.ericsson.oss.apps.ncmp.NcmpConfiguration;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import com.github.tomakehurst.wiremock.matching.UrlPathPattern;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;

import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@EnableAutoConfiguration
@SpringBootTest(properties = {"rapp-sdk.topology.base-path=http://localhost:${wiremock.server.port}/oss-core-ws/rest"},
        classes = {ClientConfig.class, NcmpConfiguration.class, TopologyConfiguration.class})
@AutoConfigureWireMock(port = 0)
public class CtsRestClientTest {

    private static final String SCENARIO = "NODE_FETCH";
    private static final UrlPathPattern NODE_TOPOLOGY_PATH  =  urlPathEqualTo("/oss-core-ws/rest/ctw/gnbdu");

    @Autowired
    private TopologyService topologyService;

    @BeforeEach
    void setup() {
        stubFor(get(NODE_TOPOLOGY_PATH).inScenario(SCENARIO)
                .whenScenarioStateIs(STARTED)
                .withQueryParams(getParameters(0L))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("response_1.json"))
                .willSetStateTo("next"));
        stubFor(get(NODE_TOPOLOGY_PATH).inScenario(SCENARIO)
                .whenScenarioStateIs("next")
                .withQueryParams(getParameters(648L))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("response_2.json"))
                .willSetStateTo("last"));
        stubFor(get(NODE_TOPOLOGY_PATH).inScenario(SCENARIO)
                .whenScenarioStateIs("last")
                .withQueryParams(getParameters(1130L))
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("response_3.json")));
    }

    @Test
    void getNodes() {
        List<ManagedObjectId> expectedFdnList = List.of(
                ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR01gNodeBRadio00039,ManagedElement=NR01gNodeBRadio00039,GNBDUFunction=1"),
                ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR01gNodeBRadio00028,ManagedElement=NR01gNodeBRadio00028,GNBDUFunction=1")
        );

        List<ManagedObjectId> fdnList = topologyService.fetchAllNodeFdn();

        Assertions.assertEquals(expectedFdnList, fdnList);
        verify(3, getRequestedFor(urlPathEqualTo("/oss-core-ws/rest/ctw/gnbdu")));
    }

    private static Map<String, StringValuePattern> getParameters(Long id) {
        return Map.of(
                "fs", equalTo("attrs"),
                "sort", equalTo("objectInstId"),
                "criteria", equalTo(String.format("(objectInstId > %dL)", id))
        );
    }
}
