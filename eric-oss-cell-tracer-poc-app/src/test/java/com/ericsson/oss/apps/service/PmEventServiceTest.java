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

import com.ericsson.oss.apps.model.embeddables.EventId;
import com.ericsson.oss.apps.model.entities.NrCellEvent;
import com.ericsson.oss.apps.model.entities.NrEvent;
import com.ericsson.oss.apps.model.entities.NrNodeEvent;
import com.ericsson.oss.apps.repository.NrCellPmEventRepository;
import com.ericsson.oss.apps.repository.NrNodePmEventRepository;
import com.ericsson.pm_event.PmEventOuterClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ericsson.pm_event.PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_CUUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class PmEventServiceTest {

    @Autowired
    public PmEventService eventService;

    @Autowired
    NrCellPmEventRepository nrCellPmEventRepository;

    @Autowired
    NrNodePmEventRepository nrNodePmEventRepository;

    @BeforeEach
    void setUp() {
        nrCellPmEventRepository.deleteAll();
        nrNodePmEventRepository.deleteAll();
    }


    /**
     * Given
     * events A,B are stored in DB and event A has timestamp T1 and event B has timestamp T2
     * when a request is sent with start time  < T1 and end time > T1 and end time < T2 and all other filters are Optional.empty()
     * then event A is returned
     */
    @Test
    void testTimeFilter() {
        var savedEvents = List.of(getNrCellEvent(0, 1), getNrCellEvent(1, 3));
        nrCellPmEventRepository.saveAll(savedEvents);
        var eventList = eventService.findFilterEvents(new EventFilters(null, null, 0, 2, List.of(), List.of(), Optional.empty(), List.of()));
        assertEquals(savedEvents.get(0), eventList.get(0));

    }

    private static NrCellEvent getNrCellEvent(long msgOffset, long timeStamp) {
        NrCellEvent nrCellEvent = new NrCellEvent();
        nrCellEvent.setId(new EventId(msgOffset, 0));
        nrCellEvent.setTimeStamp(timeStamp);
        return nrCellEvent;
    }

    private static NrNodeEvent getNrNodeEvent(long timeStamp) {
        NrNodeEvent nrNodeEvent = new NrNodeEvent();
        nrNodeEvent.setId(new EventId(0, 0));
        nrNodeEvent.setTimeStamp(timeStamp);
        return nrNodeEvent;
    }

    /**
     * Given
     * events A,B,C are stored in DB and event A is a node event
     * and event B,C are cell events.
     * and event B has NCI = 1  and event C has NCI =2
     * and all the events have timestamp T0
     * when a request is sent with start time  < T0 and end time > T0 and end time < T2 and the NCI filter has one value equal to 1
     * and all other filters are Optional.empty()
     * then events A and B are returned
     */
    @Test
    void testNciFilter() {
        var eventB = getNrCellEvent(2, 1);
        var eventC = getNrCellEvent(3, 1);
        var savedCellEvents = List.of(eventB, eventC);
        eventB.setNCI(1L);
        eventC.setNCI(2L);
        var eventA = getNrNodeEvent(1);
        nrCellPmEventRepository.saveAll(savedCellEvents);
        nrNodePmEventRepository.save(eventA);
        var eventList = eventService.findFilterEvents(new EventFilters(null, null, 0, 2, List.of(), List.of(1L), Optional.empty(), List.of()));
        var eventMap = eventList.stream().collect(Collectors.toMap(NrEvent::getId, event -> event));
        assertEquals(2, eventMap.size());
        assertTrue(eventMap.containsKey(eventA.getId()));
        assertTrue(eventMap.containsKey(eventB.getId()));
    }

    /**
     * Given
     * events A,B are stored in DB and event A is a node event
     * and event B is a cell events.
     * and event A has enodebID = 1  and event B has enodebID =2
     * and all the events have timestamp T0
     * when a request is sent with start time  < T0 and end time > T0 and end time < T2 and the enodebID filter has one value equal to 1
     * and all other filters are List.of()
     * then event A is returned
     */
    @Test
    void testGnodebIdFilter() {
        var eventA = getNrNodeEvent(1);
        eventA.setGnbId(1L);
        var eventB = getNrCellEvent(0, 1);
        eventB.setGnbId(2L);
        nrCellPmEventRepository.save(eventB);
        nrNodePmEventRepository.save(eventA);
        var eventList = eventService.findFilterEvents(new EventFilters(null, null, 0, 2, List.of(1L), List.of(), Optional.empty(), List.of()));
        assertEquals(eventA, eventList.get(0));
    }

    /**
     * Given
     * events A,B are stored in DB and event A is a node event
     * and event B is a cell event.
     * and event A has eventID = 1  and event B has eventID =2
     * and all the events have timestamp T0
     * when a request is sent with start time  < T0 and end time > T0 and end time < T2 and the eventID filter has one value equal to 1
     * and all other filters are List.of()
     * then event A is returned
     */
    @Test
    void eventIdFilter() {
        var eventA = getNrNodeEvent(1);
        eventA.setEventId(1L);
        var eventB = getNrCellEvent(0, 1);
        eventB.setEventId(2L);
        nrCellPmEventRepository.save(eventB);
        nrNodePmEventRepository.save(eventA);
        var eventList = eventService.findFilterEvents(new EventFilters(null, null, 0, 2, List.of(), List.of(), Optional.of(List.of(1L)), List.of()));
        assertEquals(eventA, eventList.get(0));
    }

    /**
     * Given
     * events A,B are stored in DB and event A is a node event
     * and event B is a cell events.
     * and event A has message group = PM_EVENT_MESSAGE_GROUP_CUUP  and event B has enodebID =PM_EVENT_MESSAGE_GROUP_DU
     * and all the events have timestamp T0
     * when a request is sent with start time  < T0 and end time > T0 and end time < T2 and message group filter has one value equal to PM_EVENT_MESSAGE_GROUP_CUUP
     * and all other filters are List.of()
     * then event A is returned
     */
    @Test
    void testNwFunctionFilter() {
        var eventA = getNrNodeEvent(1);
        eventA.setMsgGroup(PM_EVENT_MESSAGE_GROUP_CUUP);
        var eventB = getNrCellEvent(0, 1);
        eventB.setMsgGroup(PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_DU);
        nrCellPmEventRepository.save(eventB);
        nrNodePmEventRepository.save(eventA);
        var eventList = eventService.findFilterEvents(new EventFilters(null, null, 0, 2, List.of(), List.of(), Optional.empty(), List.of(PM_EVENT_MESSAGE_GROUP_CUUP)));
        assertEquals(eventA, eventList.get(0));
    }

    /**
     * Given
     *  events A,B are stored in DB and event A is a node event
     * and event A has ueTraceId = ueTr1  and event B has ueTraceId = ueTr2
     * and all the events have timestamp T0
     * when a request is sent with start time  < T0 and end time > T0 and end time < T2 and the ueTraceId filter is equal to ueTr2
     * and all other filters are List.of()
     * then event B is returned
     */
    @Test
    void testEventUeTraceFilter() {
        var eventA = getNrNodeEvent(1);
        eventA.setUeTraceId("ueTr1");
        var eventB = getNrCellEvent(0, 1);
        eventB.setUeTraceId("ueTr2");
        nrCellPmEventRepository.save(eventB);
        nrNodePmEventRepository.save(eventA);
        var eventList = eventService.findFilterEvents(new EventFilters("ueTr2", null, 0, 2, List.of(), List.of(), Optional.empty(), List.of()));
        assertEquals(1, eventList.size());
        assertEquals(eventB, eventList.get(0));
    }

    /**
     * Given
     *  events A,B are stored in DB and event A is a node event
     * and event A has traceRecordingSessionReference = trc1  and event B has traceRecordingSessionReference = trc1
     * and all the events have timestamp T0
     * when a request is sent with start time  < T0 and end time > T0 and end time < T2 and the traceRecordingSessionReference filter is equal to trc1
     * and all other filters are List.of()
     * then event A is returned
     */
    @Test
    void testEventTraceRecordingSessionReferenceFilter() {
        var eventA = getNrNodeEvent(1);
        eventA.setTraceRecordingSessionReference("trc1");
        var eventB = getNrCellEvent(0, 1);
        eventB.setTraceRecordingSessionReference("trc2");
        nrCellPmEventRepository.save(eventB);
        nrNodePmEventRepository.save(eventA);
        var eventList = eventService.findFilterEvents(new EventFilters(null, "trc1", 0, 2, List.of(), List.of(), Optional.empty(), List.of()));
        assertEquals(1, eventList.size());
        assertEquals(eventA, eventList.get(0));
    }

}