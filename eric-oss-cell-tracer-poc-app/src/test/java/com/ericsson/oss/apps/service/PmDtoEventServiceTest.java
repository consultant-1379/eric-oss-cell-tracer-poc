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

import com.ericsson.oss.apps.api.model.controller.*;
import com.ericsson.oss.apps.controller.events.nr.EventFiltersDto;
import com.ericsson.oss.apps.loader.NREventClassMap;
import com.ericsson.oss.apps.model.NREventClass;
import com.ericsson.oss.apps.model.entities.NrCellEvent;
import com.ericsson.oss.apps.model.entities.NrEvent;
import com.ericsson.oss.apps.model.mom.NRCellDU;
import com.ericsson.pm_event.CuCpPimDetectionReportOuterClass;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.ericsson.pm_event.PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_DU;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PmDtoEventServiceTest {

    @InjectMocks
    PmDtoEventService pmDtoEventService;

    @Mock
    PmEventService pmEventService;
    @Mock
    NREventClassMap nrEventClassMap;

    @Test
    void getNREvents() throws JsonProcessingException {
        NREventClass nrEventClass = new NREventClass(8L, Message.class, TrcLevel.CELL, PM_EVENT_MESSAGE_GROUP_DU, NetworkElement.RC, NetworkElement.GNODEB, MessageDirection.UNKNOWN);
        when(nrEventClassMap.getEventClass(8L)).thenReturn(Optional.of(nrEventClass));
        EventFiltersDto eventFiltersDto = new EventFiltersDto("ueTr", "trac", 1L, 2L, List.of(1L, 2L), List.of(3L, 4L), List.of(5L, 6L), List.of(NwFunction.DU), List.of(TrcLevel.CELL));
        when(nrEventClassMap.trcLevelToEIds(eventFiltersDto.eId(), List.of(TrcLevel.CELL))).thenReturn(Optional.of(List.of(8L)));

        EventFilters eventFilters = new EventFilters(
                "ueTr",
                "trac",
                1L,
                2L,
                List.of(1L, 2L),
                List.of(3L, 4L),
                Optional.of(List.of(8L)),
                List.of(PM_EVENT_MESSAGE_GROUP_DU));
        // this will check the filters are mapped (otherwise the mock won't work)
        when(pmEventService.findFilterEvents(eventFilters)).thenReturn(List.of(getNrCellEvent()));
        var eventList = pmDtoEventService.getNREvents(eventFiltersDto);
        ObjectMapper mapper = new ObjectMapper();
        String eventDto = """
                {
                  "timestamp": 1234,
                  "gnodebName": "me",
                  "NRCell": {
                    "nci": 2,
                    "cellName": "fdn"
                  },
                  "eventType": {
                    "eId": 8,
                    "eventTypeName": "Message",
                    "nwFunction": "DU",
                    "trcLevel": "CELL"
                  },
                  "messageDirection": "UNKNOWN",
                  "body": "payload",
                  "networkElement1": "RC",
                  "networkElement2": "GNODEB",
                  "asn1Content": "decoded"
                }
                """;
        NREvent expectedEvent = mapper.readValue(eventDto, NREvent.class);

        assertEquals(expectedEvent, eventList.get(0));
    }

    @Test
    void getEventTypes() {
        when(nrEventClassMap.getEventClasses()).thenReturn(List.of(new NREventClass(8L, CuCpPimDetectionReportOuterClass.CuCpPimDetectionReport.class, TrcLevel.UE, PM_EVENT_MESSAGE_GROUP_DU, NetworkElement.RC, NetworkElement.GNODEB, MessageDirection.OUTGOING)));
        var eventTypes = pmDtoEventService.getEventTypes();
        assertEquals(1, eventTypes.size());
        var eventType = eventTypes.get(0);
        assertEquals(8L, eventType.geteId());
        assertEquals("CuCpPimDetectionReport", eventType.getEventTypeName());
        assertEquals(NwFunction.DU, eventType.getNwFunction());
        assertEquals(TrcLevel.UE, eventType.getTrcLevel());
    }

    private static NrEvent getNrCellEvent() {
        NrCellEvent nrCellEvent = new NrCellEvent();
        nrCellEvent.setTimeStamp(1234L);
        nrCellEvent.setMsgGroup(PM_EVENT_MESSAGE_GROUP_DU);
        nrCellEvent.setGnbId(1L);
        nrCellEvent.setNetworkManagedElement("me");
        nrCellEvent.setPayload("payload");
        nrCellEvent.setAsnContent("decoded");
        nrCellEvent.setNCI(2L);
        nrCellEvent.setEventId(8L);
        NRCellDU nrCellDU = new NRCellDU("fdn");
        nrCellEvent.setNrCellDU(nrCellDU);
        nrCellEvent.setAsnContent("decoded");
        return nrCellEvent;
    }

}