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
package com.ericsson.oss.apps.controller.topology.nr;

import com.ericsson.oss.apps.api.controller.TopologyApi;
import com.ericsson.oss.apps.api.model.controller.Fdn;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.topology.TopologyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class TopologyApiControllerImpl implements TopologyApi {

    private final TopologyService topologyService;

    @Override
    public ResponseEntity<List<Fdn>> getAllNodeFdn() {
        return new ResponseEntity<>(
                topologyService.fetchAllNodeFdn().stream()
                        .map(this::convertToDto)
                        .toList(),
                HttpStatusCode.valueOf(200)
        );
    }

    private Fdn convertToDto(ManagedObjectId objectId) {
        return new Fdn(objectId.fetchMEId().fetchDNValue(), objectId.toString());
    }
}
