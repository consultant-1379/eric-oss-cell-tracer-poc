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
package com.ericsson.oss.apps.controller.cm.nr;

import com.ericsson.oss.apps.api.controller.CmApi;
import com.ericsson.oss.apps.api.model.controller.GNodeb;
import com.ericsson.oss.apps.api.model.controller.NRCell;
import com.ericsson.oss.apps.service.CmService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class CmApiControllerImpl implements CmApi {

    private final CmService cmService;

    @Override
    public ResponseEntity<List<GNodeb>> getGnodebs(Boolean includeCells) {
        if (includeCells) {
            return new ResponseEntity<>(cmService.getGnodebsWithCells(), HttpStatusCode.valueOf(200));
        } else {
            return new ResponseEntity<>(cmService.getGnodebs(), HttpStatusCode.valueOf(200));
        }
    }

    @Override
    public ResponseEntity<List<NRCell>> getNrcells(List<Long> gnid) {
        return new ResponseEntity<>(cmService.getNrCells(gnid), HttpStatusCode.valueOf(200));
    }
}
