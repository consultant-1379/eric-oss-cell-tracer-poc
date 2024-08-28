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

import com.ericsson.oss.apps.api.controller.EventsApi;
import com.ericsson.oss.apps.api.model.controller.NREvent;
import com.ericsson.oss.apps.api.model.controller.NRFilterValues;
import com.ericsson.oss.apps.api.model.controller.NwFunction;
import com.ericsson.oss.apps.api.model.controller.TrcLevel;
import com.ericsson.oss.apps.service.PmDtoEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class EventApiControllerImpl implements EventsApi {

    private final PmDtoEventService pmDtoEventService;

    @Override
    public ResponseEntity<List<NREvent>> getNREvents(Long start, Long end, List<Long> gnid, List<Long> nci, List<Long> eId, List<NwFunction> nwFunction, List<TrcLevel> trcLevel, String ueTraceId, String traceRecordingSessionReference) {
        EventFiltersDto eventFiltersDto = new EventFiltersDto(ueTraceId, traceRecordingSessionReference, start, end, gnid, nci, eId, nwFunction, trcLevel);
        return new ResponseEntity<>(pmDtoEventService.getNREvents(eventFiltersDto),
                HttpStatusCode.valueOf(200));
    }

    @Override
    public ResponseEntity<NRFilterValues> getNRFilterValues() {
        NRFilterValues nrFilterValues = new NRFilterValues(List.of(TrcLevel.values()), List.of(NwFunction.values()), pmDtoEventService.getEventTypes());
        return new ResponseEntity<>(nrFilterValues,
                HttpStatusCode.valueOf(200));
    }

}
