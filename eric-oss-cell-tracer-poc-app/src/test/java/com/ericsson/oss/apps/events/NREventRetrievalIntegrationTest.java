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

import com.ericsson.oss.apps.api.model.controller.NetworkElement;
import com.ericsson.oss.apps.events.decoder.Asn1DecoderService;
import com.ericsson.oss.apps.kafka.consumer.NREventDataRetrieval;
import com.ericsson.oss.apps.kafka.producer.RandomEventDataSender;
import com.ericsson.oss.apps.loader.AllowedNodeMap;
import com.ericsson.oss.apps.loader.NREventClassMap;
import com.ericsson.oss.apps.model.NREventClass;
import com.ericsson.oss.apps.model.entities.NrEvent;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.service.SubscriptionService;
import com.ericsson.pm_event.DuFunctionPmEventOuterClass;
import com.ericsson.pm_event.DuPimMeasurementsRepOuterClass;
import com.ericsson.pm_event.PmEventOuterClass;
import com.google.protobuf.ByteString;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.ericsson.oss.apps.api.model.controller.TrcLevel.CELL;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest(properties = {"rapp-sdk.kafka.enabled=true", "app.generator.events.enabled=true", "app.data.config=classpath:"})
class NREventRetrievalIntegrationTest {
    public static final String DECODED = "{ \"decoded\" : \"\\\\n this is not a valid json\" }";
    @SpyBean
    private NREventDataRetrieval nrEventDataRetrieval;
    @SpyBean
    private NREventDeserializer nrEventDeserializer;
    @SpyBean
    private NREventDecoder nrEventDecoder;
    @SpyBean
    Asn1DecoderService asn1DecoderService;

    @Autowired
    private NREventClassMap nrEventClassMap;
    // used by simulator
    @Autowired
    private AllowedNodeMap allowedNodeMap;
    @MockBean
    private SubscriptionService subscriptionService;
    @Autowired
    private KafkaTemplate<String, byte[]> nrEventTemplate;
    @Autowired
    private RandomEventDataSender randomEventDataSender;

    @MockBean
    private EventProcessor<NrEvent> eventProcessor;

    @Captor
    private ArgumentCaptor<List<NrEvent>> nrEventListArgumentCaptor;

    @Captor
    private ArgumentCaptor<List<byte[]>> asn1DecodingArgumentCaptor;

    @Container
    static final KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.3.3")
    );

    private final String FDN = "SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR01gNodeBRadio00020,ManagedElement=NR01gNodeBRadio00020,GNBDUFunction=1";

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("rapp-sdk.kafka.bootstrapServers", kafka::getBootstrapServers);
        registry.add("spring.kafka.schema-registry.url", () -> "mock://testurl");
    }

    @BeforeEach
    public void setup() {
        allowedNodeMap.getAllNodes().forEach(node -> when(subscriptionService.isGnodebAllowed(node)).thenReturn(true));
        nrEventClassMap.putEventClass(new NREventClass(2062, DuPimMeasurementsRepOuterClass.DuPimMeasurementsRep.class, CELL, PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_DU, NetworkElement.RC, NetworkElement.GNODEB, null));
    }


    /**
     * This looks overkill however since the data is sent through
     * a kafka protobuf serializer the magic byte(s) is set and unless we want to
     * set that manually the easiest option is to and send the message over the wire.
     */
    @Test
    void decodePimDEventOkPayload() {
        when(subscriptionService.isEventIdAllowed(anyLong())).thenReturn(true);
        when(asn1DecoderService.decode(anyList())).thenReturn(List.of("\\n this is not a valid json"));

        randomEventDataSender.sendNREvent();
        var pmEvent = randomEventDataSender.getLastSentPMEvent();

        await()
                .pollInterval(Duration.ofMillis(300))
                .atMost(3, SECONDS)
                .untilAsserted(() -> {
                    verify(eventProcessor, atLeastOnce()).processEvents(nrEventListArgumentCaptor.capture());
                    verify(asn1DecoderService, atLeastOnce()).decode(asn1DecodingArgumentCaptor.capture());
                    assertArrayEquals(pmEvent.toByteArray(), asn1DecodingArgumentCaptor.getValue().get(0));
                    List<NrEvent> events = nrEventListArgumentCaptor.getValue();
                    assertEquals(1, events.size(), "unexpected number of records received");
                    Integer ueTraceId = Integer.valueOf(new String(Base64.getDecoder().decode(events.get(0).getUeTraceId())));
                    assertNotNull(ueTraceId);
                    assertEquals(DECODED, events.get(0).getAsnContent());
                    assertNotNull(events.get(0).getPayload());
                });
    }

    @Test
    void decodePimDEventWrongPayload() {
        when(subscriptionService.isEventIdAllowed(anyLong())).thenReturn(true);
        sendNREvent(ByteString.copyFrom(new byte[]{-1}));
        await()
                .pollInterval(Duration.ofMillis(300))
                .atMost(3, SECONDS)
                .untilAsserted(() -> {
                    verify(subscriptionService, atLeastOnce()).isGnodebAllowed(ManagedObjectId.of(FDN));
                    verify(subscriptionService, atLeastOnce()).isEventIdAllowed(2062);
                    verify(nrEventDeserializer, atLeastOnce()).deserialize(any());
                    verifyNoInteractions(nrEventDecoder);
                    verifyNoInteractions(eventProcessor);
                });
    }

    /**
     * Verify all the filter conditions are applied to the incoming messages.
     *
     * @param eventIdHeaderKey header key of event id
     * @param eventId          eventId of sent message
     * @param node             fdn of sent message
     */
    @ParameterizedTest
    @CsvSource(value = {
            "wrongHeaderKey, 2062, \"SubNetwork=Athlone,MeContext=MA2B0001A2,ManagedElement=MA2B0001A2,GNBDUFunction=1,NRCellDU=JA2B0001A11\"",
            "event_id, 2061, \"SubNetwork=Athlone,MeContext=MA2B0001A2,ManagedElement=MA2B0001A2,GNBDUFunction=1,NRCellDU=JA2B0001A11\"",
            "event_id, 2062, \"SubNetwork=Athlone,MeContext=MA2B0001A2,ManagedElement=gino,GNBDUFunction=colluc,NRCellDU=cello\""
    })
    void decodePimDEventFilteredOut(String eventIdHeaderKey, String eventId, String node) {
        when(subscriptionService.isEventIdAllowed(2062)).thenReturn(true);
        DuPimMeasurementsRepOuterClass.DuPimMeasurementsRep pimdCtrRecord = DuPimMeasurementsRepOuterClass.DuPimMeasurementsRep.getDefaultInstance();
        DuFunctionPmEventOuterClass.DuFunctionPmEvent duFunctionPmEvent = DuFunctionPmEventOuterClass.DuFunctionPmEvent.newBuilder().setDuPimMeasurementsRep(pimdCtrRecord).build();
        sendNREvent(duFunctionPmEvent.toByteString(), eventIdHeaderKey, eventId, node);
        await()
                .pollInterval(Duration.ofMillis(300))
                .atMost(3, SECONDS)
                .untilAsserted(() -> {
                    verify(nrEventDataRetrieval, atLeastOnce()).retrieveBatchData(any());
                    verify(subscriptionService, atLeastOnce()).isGnodebAllowed(any());
                    verifyNoInteractions(nrEventDeserializer);
                    verifyNoInteractions(nrEventDecoder);
                    verifyNoInteractions(eventProcessor);
                });
    }


    void sendNREvent(ByteString payload) {
        sendNREvent(payload, "event_id", "2062", FDN);
    }

    void sendNREvent(ByteString payload, String eventIdHeaderKey, String eventId, String node) {
        PmEventOuterClass.PmEventHeader pmEventHeader = PmEventOuterClass.PmEventHeader.newBuilder()
                .getDefaultInstanceForType();
        PmEventOuterClass.PmEvent ctrRecord = PmEventOuterClass.PmEvent.newBuilder()
                .setHeader(pmEventHeader)
                .setPayload(payload)
                .build();
        List<Header> headers = new ArrayList<>();
        headers.add(new RecordHeader(eventIdHeaderKey, eventId.getBytes()));
        ProducerRecord<String, byte[]> record = new ProducerRecord<>("5g-pm-event-file-transfer-and-processing--enm1", null, node, ctrRecord.toByteArray(), headers);
        nrEventTemplate.send(record);
    }

}