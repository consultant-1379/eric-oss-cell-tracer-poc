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
package com.ericsson.oss.apps.cleaner;

import com.ericsson.oss.apps.model.embeddables.EventId;
import com.ericsson.oss.apps.model.entities.NrCellEvent;
import com.ericsson.oss.apps.model.entities.NrNodeEvent;
import com.ericsson.oss.apps.repository.NrCellPmEventRepository;
import com.ericsson.oss.apps.repository.NrNodePmEventRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {"pm.ropTimeExpirePeriod=2"})
class PmEventCleanerTest {
    @Autowired
    private NrNodePmEventRepository nrNodePmEventRepository;

    @Autowired
    private NrCellPmEventRepository nrCellPmEventRepository;

    @Autowired
    private PmEventCleaner pmEventCleaner;

    @Test
    @Transactional
    void testCleanPmEntities() {
        Long oldRopTime = 1704139200000L;
        Long now = System.currentTimeMillis();

        nrCellPmEventRepository.saveAll(List.of(getNrCellEvent(0, oldRopTime), getNrCellEvent(1, now)));
        nrNodePmEventRepository.saveAll(List.of(getNrNodeEvent(0, oldRopTime), getNrNodeEvent(1, now)));

        pmEventCleaner.cleanPmEvents();

        assertEquals(1, nrCellPmEventRepository.findAll().size());
        assertEquals(1, nrNodePmEventRepository.findAll().size());

    }

    @Test
    void testGetRopTime() {
        Long sysTime = 1704141433000L;
        Long shiftedRopTime = 1704139200000L;
        assertEquals(shiftedRopTime, pmEventCleaner.getRopTime(sysTime, 2));
    }

    private static NrCellEvent getNrCellEvent(long msgOffset, long timeStamp) {
        NrCellEvent nrCellEvent = new NrCellEvent();
        nrCellEvent.setId(new EventId(msgOffset, 0));
        nrCellEvent.setTimeStamp(timeStamp);
        return nrCellEvent;
    }

    private static NrNodeEvent getNrNodeEvent(long msgOffset, long timeStamp) {
        NrNodeEvent nrNodeEvent = new NrNodeEvent();
        nrNodeEvent.setId(new EventId(msgOffset, 0));
        nrNodeEvent.setTimeStamp(timeStamp);
        return nrNodeEvent;
    }

}
