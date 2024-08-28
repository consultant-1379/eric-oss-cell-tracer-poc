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
package com.ericsson.oss.apps.service;

import com.ericsson.oss.apps.api.model.controller.GNodeb;
import com.ericsson.oss.apps.api.model.controller.NRCell;
import com.ericsson.oss.apps.model.mom.GNBDUFunction;
import com.ericsson.oss.apps.model.mom.NRCellDU;
import com.ericsson.oss.apps.repository.CmGNBDUFunctionRepo;
import com.ericsson.oss.apps.repository.CmNrCellDuRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Fetches CM (nodes and cells) from the DB and maps to DTOs.
 */
@Service
@RequiredArgsConstructor
public class CmService {
    private final CmGNBDUFunctionRepo cmGNodeBRepo;
    private final CmNrCellDuRepo cmNrCellDuRepo;

    public List<GNodeb> getGnodebs() {
        return cmGNodeBRepo.findAll().stream()
                .map(this::convertToGNodeb)
                .toList();
    }

    @Transactional
    public List<GNodeb> getGnodebsWithCells() {
        Map<GNBDUFunction, List<NRCellDU>> result = cmNrCellDuRepo.findAll().stream()
                .collect(Collectors.groupingBy(NRCellDU::getGNodeB));
        return result.entrySet().stream()
                .map(e -> convertToGNodeb(e.getKey(), e.getValue()))
                .toList();
    }

    @Transactional
    public List<NRCell> getNrCells(List<Long> gnid) {
        return cmNrCellDuRepo.findBygNodeB_gNBIdIn(gnid).stream()
                .map(nrCellDU -> new NRCell(nrCellDU.getNCI(), nrCellDU.getObjectId().fetchDNValue()))
                .toList();
    }

    private GNodeb convertToGNodeb(GNBDUFunction gnodeb, List<NRCellDU> cells) {
        GNodeb node = convertToGNodeb(gnodeb);
        node.cells(cells.stream().map(this::convertToNRCell).toList());
        return node;
    }

    private GNodeb convertToGNodeb(GNBDUFunction gnodeb) {
        return new GNodeb(gnodeb.getGNBId(), gnodeb.getObjectId().fetchMEId().fetchDNValue());
    }

    private NRCell convertToNRCell(NRCellDU nrCellDU) {
        return new NRCell(nrCellDU.getNCI(), nrCellDU.getObjectId().fetchDNValue());
    }
}