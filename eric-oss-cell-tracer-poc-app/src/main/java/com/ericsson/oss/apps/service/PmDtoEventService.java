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

import com.ericsson.oss.apps.api.model.controller.EventType;
import com.ericsson.oss.apps.api.model.controller.NRCell;
import com.ericsson.oss.apps.api.model.controller.NREvent;
import com.ericsson.oss.apps.api.model.controller.TrcLevel;
import com.ericsson.oss.apps.controller.events.nr.EventFiltersDto;
import com.ericsson.oss.apps.loader.NREventClassMap;
import com.ericsson.oss.apps.model.converters.PmEventMessageGroupMapper;
import com.ericsson.oss.apps.model.entities.NrCellEvent;
import com.ericsson.oss.apps.model.entities.NrEvent;
import com.ericsson.oss.apps.model.mom.NRCellDU;
import com.ericsson.pm_event.PmEventOuterClass;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Maps trace levels to event IDs, and entities to DTOS.
 */
@Service
@RequiredArgsConstructor
public class PmDtoEventService {

    private final PmEventService pmEventService;
    private final NREventClassMap nrEventClassMap;

    public List<NREvent> getNREvents(EventFiltersDto eventFiltersDto) {
        List<TrcLevel> traceLevelList = eventFiltersDto.trcLevel().stream().toList();
        Optional<List<Long>> eIdList = nrEventClassMap.trcLevelToEIds(eventFiltersDto.eId(), traceLevelList);
        List<PmEventOuterClass.PmEventMessageGroup> pmEventMessageGroups = eventFiltersDto.nwFunction().stream()
                .map(PmEventMessageGroupMapper::nwFunctionDtoToEntity).toList();

        return findFilterCellEvents(new EventFilters(
                eventFiltersDto.ueTraceId(),
                eventFiltersDto.traceRecordingSessionReference(),
                eventFiltersDto.start(),
                eventFiltersDto.end(),
                eventFiltersDto.gnid(),
                eventFiltersDto.nci(),
                eIdList,
                pmEventMessageGroups));
    }


    private List<NREvent> findFilterCellEvents(EventFilters eventFilters) {
        var eventEntityList = pmEventService.findFilterEvents(eventFilters);
        return eventEntityList.stream().map(this::eventEntityToDto).toList();
    }

    private NREvent eventEntityToDto(NrEvent eventEntity) {
        NREvent nrEvent = new NREvent();
        EventType eventType = new EventType();
        nrEventClassMap.getEventClass(eventEntity.getEventId()).ifPresent(eventClass -> {
            eventType.setEventTypeName(eventClass.getEventClassName());
            eventType.setTrcLevel(eventClass.traceLevel());
            nrEvent.setNetworkElement1(eventClass.networkElement1());
            nrEvent.setNetworkElement2(eventClass.networkElement2());
            nrEvent.setMessageDirection(eventClass.messageDirection());
        });
        eventType.seteId(eventEntity.getEventId());
        eventType.setNwFunction(PmEventMessageGroupMapper.nwFunctionEntityToDto(eventEntity.getMsgGroup()));
        nrEvent.setEventType(eventType);
        nrEvent.setBody(eventEntity.getPayload());
        nrEvent.setTimestamp(eventEntity.getTimeStamp());
        nrEvent.setGnodebName(eventEntity.getNetworkManagedElement());
        nrEvent.setTraceRecordingSessionReference(eventEntity.getTraceRecordingSessionReference());
        nrEvent.setUeTraceId(eventEntity.getUeTraceId());
        nrEvent.setAsn1Content(eventEntity.getAsnContent());

        if (eventEntity instanceof NrCellEvent nrCellEvent) {
            NRCell nrCell = new NRCell();
            nrCell.setNci(nrCellEvent.getNCI());
            Optional.ofNullable(nrCellEvent.getNrCellDU())
                    .map(NRCellDU::getFdn)
                    .ifPresent(nrCell::setCellName);
            nrEvent.setNrCell(nrCell);
        }
        return nrEvent;
    }

    public List<EventType> getEventTypes() {
        return nrEventClassMap.getEventClasses().stream().map(nrEventClass ->
                new EventType(nrEventClass.eventId(),
                        nrEventClass.getEventClassName(),
                        PmEventMessageGroupMapper.nwFunctionEntityToDto(nrEventClass.pmEventMessageGroup()),
                        nrEventClass.traceLevel())).toList();
    }


}