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
package com.ericsson.oss.apps.events;

import com.ericsson.oss.apps.model.embeddables.EtcmVersionHeader;
import com.ericsson.oss.apps.model.embeddables.EventId;
import com.ericsson.oss.apps.model.embeddables.PmEventVersionHeader;
import com.ericsson.oss.apps.model.entities.NrCellEvent;
import com.ericsson.oss.apps.model.entities.NrEvent;
import com.ericsson.oss.apps.model.entities.NrNodeEvent;
import com.ericsson.pm_event.PmEventOuterClass;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class NREventDecoder {

    private final NREventMethodCache nrEventMethodCache;

    private final JsonFormat.Printer jsonFormatPrinter = JsonFormat.printer();

    public Optional<NREventBytes> decode(DeserializedNREventRecord eventRecord) {
        return initEvent(eventRecord.getMessagePayload(), eventRecord.getPayloadType())
                .map(event -> setId(event, eventRecord.getId()))
                .flatMap(event -> setNodeInfo(event, eventRecord.getMessagePayload(), eventRecord.getPayloadType()))
                .flatMap(event -> setPayload(event, eventRecord.getMessagePayload()))
                .map(event -> new NREventBytes(mapOuterClass(event, eventRecord.getMessage()), eventRecord.getMessageBytes()));
    }


    private Optional<NrEvent> initEvent(Message payloadEvent, Class<?> payloadType) {
        Optional<Method> getNci = nrEventMethodCache.getNci(payloadType);
        if (getNci.isPresent()) {
            NrCellEvent cellEvent = new NrCellEvent();
            Method method = getNci.get();
            try {
                cellEvent.setNCI((Long) method.invoke(payloadEvent));
                return Optional.of(cellEvent);
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.warn("NCI method exists, but couldn't invoke", e);
            }
            return Optional.empty();
        } else {
            return Optional.of(new NrNodeEvent());
        }
    }

    private static NrEvent setId(NrEvent event, EventId id) {
        event.setId(id);
        return event;
    }

    private Optional<NrEvent> setNodeInfo(NrEvent event, Message payloadEvent, Class<?> payloadType) {
        try {
            Method getMainPlmnId = nrEventMethodCache.getMainPlmnId(payloadType);
            event.setMainPlmnId(((ByteString) getMainPlmnId.invoke(payloadEvent)).toByteArray());
            Method getGnbId = nrEventMethodCache.getGnbId(payloadType);
            event.setGnbId((Long) getGnbId.invoke(payloadEvent));
            Method getGnbIdLength = nrEventMethodCache.getGnbIdLength(payloadType);
            event.setGnbIdLength((Long) getGnbIdLength.invoke(payloadEvent));
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.warn("No NodeId found, can't map to a ManagedObject", e);
            return Optional.empty();
        }
        return Optional.of(event);
    }

    private Optional<NrEvent> setPayload(NrEvent event, Message payloadEvent) {
        try {
            event.setPayload(jsonFormatPrinter.print(payloadEvent));
        } catch (InvalidProtocolBufferException e) {
            log.warn("Payload serialization failed, Message can't be stored as a JSON", e);
            return Optional.empty();
        }
        return Optional.of(event);
    }

    private static NrEvent mapOuterClass(NrEvent event, PmEventOuterClass.PmEvent decodedEvent) {
        event.setEventId(decodedEvent.getEventId());
        var header = decodedEvent.getHeader();
        event.setTimeStamp(header.getTimeStamp());
        event.setSystemUuid(header.getSystemUuid().toByteArray());
        event.setUeTraceId(Base64.getEncoder().encodeToString(header.getUeTraceId().toByteArray()));
        event.setTraceReference(header.getTraceReference().toByteArray());
        event.setTraceRecordingSessionReference(Base64.getEncoder().encodeToString(header.getTraceRecordingSessionReference().toByteArray()));
        event.setComputeName(header.getComputeName());
        event.setNetworkManagedElement(header.getNetworkManagedElement());
        event.setMsgGroup(decodedEvent.getGroup());
        event.setPmEventVersionHeader(new PmEventVersionHeader(decodedEvent.getPmEventGroupVersion(), decodedEvent.getPmEventCommonVersion(), decodedEvent.getPmEventCorrectionVersion()));
        event.setEtcmVersionHeader(new EtcmVersionHeader(decodedEvent.getEtcmVersion(), decodedEvent.getEtcmCorrectionVersion()));
        return event;
    }
}
