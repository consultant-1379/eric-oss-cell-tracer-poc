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

import com.ericsson.oss.apps.CoreApplicationTest;
import com.ericsson.oss.apps.model.mom.NRSectorCarrier;
import com.ericsson.oss.apps.ncmp.NcmpClient;
import com.ericsson.oss.apps.ncmp.model.ManagedObject;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@SpringBootTest(properties = {
        "rapp-sdk.ncmp.base-path=https://localhost:${wiremock.server.https-port}/ncmp",
        "spring.security.oauth2.client.registration.eic.client-id=client-id",
        "spring.security.oauth2.client.registration.eic.client-secret=client-secret",
        "spring.security.oauth2.client.provider.eic.token-uri=https://localhost:${wiremock.server.https-port}/token"
}, classes = {WireMockConfig.class, CoreApplicationTest.class})
class OAuthRestIntegrationTest {
    private static final String CM_HANDLE = "A1D3510D87DD176BFDDEE7E4541A848B";
    private static final String ME_FDN = "SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR123gNodeBRadio00019,ManagedElement=NR123gNodeBRadio00019";
    private static final String SECTOR_CARRIER_FDN = ME_FDN + ",GNBDUFunction=1,NRSectorCarrier=1";
    private static final ManagedObjectId SECTOR_CARRIER_RESOURCE = ManagedObjectId.of(SECTOR_CARRIER_FDN);

    private static final Map<String, StringValuePattern> QUERY_PARAMETERS = Map.of(
            "resourceIdentifier", equalTo("/"),
            "options", equalTo("fields=NRSectorCarrier/attributes(*)")
    );

    @Autowired
    private WireMockServer mockServer;
    @Autowired
    private NcmpClient ncmpClient;

    @Test
    void getCmResource() {
        var tokenUrlPattern = urlPathEqualTo("/token");
        var ncmpUrlPattern = urlPathMatching(String.format("/ncmp/v1/ch/%s/data/ds/ncmp-datastore.*", CM_HANDLE));
        String bearerToken = "Bearer your-access-token";

        mockServer.stubFor(post(tokenUrlPattern)
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBodyFile("oauth/jwt_token.json")));

        mockServer.stubFor(get(ncmpUrlPattern)
                .withQueryParams(QUERY_PARAMETERS)
                .willReturn(aResponse()
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withHeader(AUTHORIZATION, bearerToken)
                        .withBodyFile("oauth/resources.json")));

        Optional<NRSectorCarrier> optionalSectorCarrier = ncmpClient.getCmResource(SECTOR_CARRIER_RESOURCE, NRSectorCarrier.class);
        mockServer.verify(postRequestedFor(tokenUrlPattern));
        mockServer.verify(getRequestedFor(ncmpUrlPattern).withHeader(AUTHORIZATION, equalTo(bearerToken)));
        assertThat(optionalSectorCarrier.map(ManagedObject::getObjectId)).hasValue(SECTOR_CARRIER_RESOURCE);
    }

}
