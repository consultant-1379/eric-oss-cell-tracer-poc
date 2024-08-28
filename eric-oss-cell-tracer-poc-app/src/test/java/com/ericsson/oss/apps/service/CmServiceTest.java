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
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.repository.CmGNBDUFunctionRepo;
import com.ericsson.oss.apps.repository.CmNrCellDuRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class CmServiceTest {

    private static final String NODE_FDN = "SubNetwork=Ireland,MeContext=NR03gNodeBRadio00002,ManagedElement=NR03gNodeBRadio00001";
    private static final NRCell TEST_CELL = new NRCell(234L, "NR01gNodeBRadio00002-1");
    private static final GNodeb TEST_NODE = new GNodeb(1234L, "NR03gNodeBRadio00001");

    @Autowired
    public CmService cmService;

    @Autowired
    CmNrCellDuRepo cmNrCellDuRepo;

    @Autowired
    CmGNBDUFunctionRepo cmGNodeBRepo;

    @BeforeEach
    void setup() {
        cmNrCellDuRepo.deleteAll();
        cmGNodeBRepo.deleteAll();
        GNBDUFunction gNodeB = new GNBDUFunction();
        gNodeB.setObjectId(ManagedObjectId.of(NODE_FDN + ",GNBDUFunction=1"));
        gNodeB.setGNBId(1234L);
        NRCellDU nrCellDU = new NRCellDU();
        nrCellDU.setObjectId(ManagedObjectId.of(NODE_FDN + ",GNBDUFunction=1,NRCellDU=NR01gNodeBRadio00002-1"));
        nrCellDU.setNCI(234L);
        cmGNodeBRepo.save(gNodeB);
        cmNrCellDuRepo.save(nrCellDU);
    }

    @Test
    void getGnodebs() {
        var gnodebList = cmService.getGnodebs();
        assertEquals(TEST_NODE, gnodebList.get(0));
    }

    @Test
    void getGnodebsWithCells() {
        var gnodeb = cmService.getGnodebsWithCells().get(0);
        assertEquals(TEST_NODE.getGnid(), gnodeb.getGnid());
        assertEquals(TEST_NODE.getGnodebName(), gnodeb.getGnodebName());
        assertEquals(TEST_CELL, gnodeb.getCells().get(0));
    }

    @Test
    void getNrCells() {
        var nrCellList = cmService.getNrCells(List.of(1234L));
        assertEquals(TEST_CELL, nrCellList.get(0));
        assertTrue(cmService.getNrCells(List.of(123L)).isEmpty());
    }
}