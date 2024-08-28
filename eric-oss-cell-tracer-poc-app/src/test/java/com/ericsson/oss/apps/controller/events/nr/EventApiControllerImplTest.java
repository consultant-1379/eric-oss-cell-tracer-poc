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
package com.ericsson.oss.apps.controller.events.nr;

import com.ericsson.oss.apps.api.model.controller.*;
import com.ericsson.oss.apps.service.PmDtoEventService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(EventApiControllerImpl.class)
class EventApiControllerImplTest {

    public static final String DECODED = "{\"decoded\" : \"ASN1\"}";
    @MockBean
    private WebClient webClient;

    @MockBean
    PmDtoEventService pmDtoEventService;

    @Autowired
    private MockMvc mvc;

    @Test
    void nrEventCheck() throws Exception {
        EventFiltersDto eventFiltersDto = new EventFiltersDto("trace", "reference",1L, 2L, List.of(1L, 2L), List.of(3L, 4L), List.of(5L, 6L), List.of(NwFunction.DU), List.of(TrcLevel.CELL));
        NREvent nrEvent = getNrEvent();
        var eventList = new ArrayList<NREvent>();
        eventList.add(nrEvent);
        Mockito.when(pmDtoEventService.getNREvents(eventFiltersDto)).thenReturn(eventList);
        mvc.perform(get("/v1/event/nr/events")
                        .queryParam("ueTraceId", "trace")
                        .queryParam("traceRecordingSessionReference", "reference")
                        .queryParam("start", "1")
                        .queryParam("end", "2")
                        .queryParam("gnid", "1", "2")
                        .queryParam("nci", "3", "4")
                        .queryParam("eId", "5", "6")
                        .queryParam("nwFunction", NwFunction.DU.toString())
                        .queryParam("trcLevel", TrcLevel.CELL.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("""
                        [{"timestamp":1,"gnodebName":"a gnodeb","eventType":{"eId":1,"eventTypeName":"a class name","nwFunction":"DU","trcLevel":"CELL"},"messageDirection":"OUTGOING","traceRecordingSessionReference":"reference","ueTraceId":"trace","body":"event body","networkElement1":"RC","networkElement2":"PP","asn1Content":"{\\"decoded\\" : \\"ASN1\\"}"}]"""));
        verify(pmDtoEventService, times(1)).getNREvents(eventFiltersDto);
    }

    @NotNull
    private static NREvent getNrEvent() {
        NREvent nrEvent = new NREvent();
        EventType eventType = new EventType();
        eventType.seteId(1L);
        eventType.setNwFunction(NwFunction.DU);
        eventType.setTrcLevel(TrcLevel.CELL);
        eventType.setEventTypeName("a class name");
        nrEvent.setEventType(eventType);
        nrEvent.setTimestamp(1L);
        nrEvent.setGnodebName("a gnodeb");
        nrEvent.setMessageDirection(MessageDirection.OUTGOING);
        nrEvent.setBody("event body");
        nrEvent.setUeTraceId("trace");
        nrEvent.setTraceRecordingSessionReference("reference");
        nrEvent.setNetworkElement1(NetworkElement.RC);
        nrEvent.setNetworkElement2(NetworkElement.PP);
        nrEvent.setAsn1Content(DECODED);
        return nrEvent;
    }

    @Test
    void getNRFilterValues() throws Exception {
        EventType eventType = new EventType(1L, "a class name", NwFunction.DU, TrcLevel.CELL);
        org.mockito.Mockito.when(pmDtoEventService.getEventTypes()).thenReturn(List.of(eventType));
        mvc.perform(get("/v1/event/nr/filters")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("""
                       {"trcLevels":["GNODEB","CELL","UE"],"nwFunctions":["NO_VALUE","DU","CUCP","CUUP","UNRECOGNIZED"],"eventTypes":[{"eId":1,"eventTypeName":"a class name","nwFunction":"DU","trcLevel":"CELL"}]}"""));
        verify(pmDtoEventService, times(1)).getEventTypes();
    }

}