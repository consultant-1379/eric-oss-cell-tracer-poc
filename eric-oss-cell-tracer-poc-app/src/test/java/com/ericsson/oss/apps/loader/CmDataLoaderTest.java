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
package com.ericsson.oss.apps.loader;

import com.ericsson.oss.apps.CoreApplicationTest;
import com.ericsson.oss.apps.config.CmDataLoaderConf;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.CmGNBDUFunctionRepo;
import com.ericsson.oss.apps.repository.CmNrCellDuRepo;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AutoConfigureWireMock(port = 0)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "rapp-sdk.ncmp.base-path=http://localhost:${wiremock.server.port}/ncmp",
        "app.scheduling.enable=false"
}, classes = {CmDataLoaderConf.class, CoreApplicationTest.class})
class CmDataLoaderTest {

    @Autowired
    private CmNrCellDuRepo cellDuRepo;
    @Autowired
    private CmGNBDUFunctionRepo cmGNBDUFunctionRepo;
    @Autowired
    private CmDataLoader cmDataLoader;

    private static final ManagedObjectId CELL_RESOURCE = ManagedObjectId.of("SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR03gNodeBRadio00002,ManagedElement=NR03gNodeBRadio00002,GNBDUFunction=1,NRCellDU=NR03gNodeBRadio00002-2");
    private static final Map<String, StringValuePattern> QUERY_PARAMETERS = Map.of(
            "resourceIdentifier", equalTo("/"),
            "options", matching("fields=.+")
    );

    @Test
    @Order(1)
    void cmLoaderWithEmptyNodeList() {
        cmDataLoader.extractCmInfo(List.of());
        assertRepo(0, 0);
    }

    @Test
    @Order(2)
    void cmLoaderWithNode() {
        var ncmpUrlPattern = urlPathEqualTo("/ncmp/v1/ch/92F1CB35798FD7D13BCC6FF825D89CD6/data/ds/ncmp-datastore%3Apassthrough-running");

        stubFor(get(ncmpUrlPattern)
                .withQueryParams(QUERY_PARAMETERS)
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("cm_resources.json")));

        cmDataLoader.extractCmInfo(List.of(CELL_RESOURCE));

        assertRepo(2, 1);
        cellDuRepo.findAll().forEach(nrCellDU -> {
            assertNotNull(nrCellDU.getGNodeB());
            assertEquals(cmGNBDUFunctionRepo.findAll().get(0), nrCellDU.getGNodeB());
        });
    }

    private void assertRepo(int cellCount, int nodeCount) {
        assertEquals(cellCount, cellDuRepo.findAll().size());
        assertEquals(nodeCount, cmGNBDUFunctionRepo.findAll().size());
    }
}
