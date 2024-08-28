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

package com.ericsson.oss.apps.kafka.consumer;

import com.ericsson.oss.apps.events.EventProcessor;
import com.ericsson.oss.apps.events.NREventDecoder;
import com.ericsson.oss.apps.events.NREventDeserializer;
import com.ericsson.oss.apps.events.decoder.Asn1DecoderService;
import com.ericsson.oss.apps.loader.NREventClassMap;
import com.ericsson.oss.apps.model.entities.NrEvent;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.service.SubscriptionService;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Slf4j
@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "rapp-sdk.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
public class NREventDataRetrieval extends CustomBatchMessageListener {

    final EventProcessor<NrEvent> eventProcessor;
    final NREventDeserializer nrEventDeserializer;
    final NREventDecoder nrEventDecoder;
    final NREventClassMap nrEventClassMap;
    final SubscriptionService subscriptionService;
    public static final String EVENT_ID_HEADER_KEY = "event_id";
    public static final Map<String, Optional<Set<String>>> HEADER_MAP = Map.of(EVENT_ID_HEADER_KEY, Optional.empty());

    private final Counter kafkaNREventBatches;
    private final Counter kafkaNREventValidHeader;
    private final Counter kafkaNREventValidNode;
    private final Counter kafkaNREventValidId;
    private final Counter kafkaNREventAllowedId;
    private final Asn1DecoderService asn1DecoderService;

    /**
     * 0. Filters out records with invalid/missing header value
     * 1. Parses the records into protobuf
     * 2. Maps them to the internal data model
     * 3. saves them to DB
     * 4. propagates an internal event with the event IDs
     *
     * @param records incoming records from Kafka, this class expects valid protobuf for a specific event id
     */
    @Override
    public void retrieveBatchData(List<ConsumerRecord<String, byte[]>> records) {
        if (records == null) {
            return;
        }
        kafkaNREventBatches.increment();
        var events = records.parallelStream()
                .peek(kafkaRecord -> observeKafkaRecord("Received event ", kafkaRecord, kafkaNREventValidHeader))
                .filter(kafkaRecord -> subscriptionService.isGnodebAllowed(ManagedObjectId.of(kafkaRecord.key())))
                .peek(kafkaRecord -> observeKafkaRecord("Allowed GNodeB ", kafkaRecord, kafkaNREventValidNode))
                .flatMap(this::getEventClassAndKafkaRecordStream)
                .flatMap(eventClassAndKafkaRecord -> nrEventDeserializer.deserialize(eventClassAndKafkaRecord).stream())
                .flatMap(deserializedRecord -> nrEventDecoder.decode(deserializedRecord).stream())
                .map(nrEventBytes -> {
                    nrEventBytes.getNrEvent().setAsnContent(wrapInJson(getDecoded(nrEventBytes.getMessageBytes())));
                    return nrEventBytes.getNrEvent();
                })
                .toList();
        if (!events.isEmpty()) {
            eventProcessor.processEvents(events);
        }
    }

    /**
     * Strips magic bytes and schema indices and sends to decoder. This only works since there is a single schema for the outer event!
     *
     * @param messageBytes byte array as it comes from Kafka (with schema information)
     * @return the decoded message in txt format
     */
    private String getDecoded(byte[] messageBytes) {
        return asn1DecoderService.decode(List.of(messageBytes)).get(0);
    }

    /**
     * temporary wrapping to store the decoded ASN.1 in JSON column in PG.
     * once the decoder returns valid JSON we can remove it
     *
     * @param decoded the decoded ASN.1 string (not JSON compliant)
     * @return decoded ASN.1 wrapped in JSON
     */
    private String wrapInJson(String decoded) {
        return "{ \"decoded\" : \"%s\" }".formatted(StringEscapeUtils.escapeJson(decoded));
    }

    @NotNull
    private Stream<EventClassAndKafkaRecord> getEventClassAndKafkaRecordStream(ConsumerRecord<String, byte[]> kafkaRecord) {
        return getEventId(kafkaRecord.headers())
                .stream()
                .peek(eventClassKafkaRecord -> observeKafkaRecord("Valid eventID ", kafkaRecord, kafkaNREventValidId))
                .filter(subscriptionService::isEventIdAllowed)
                .peek(eventClassKafkaRecord -> observeKafkaRecord("Allowed eventID ", kafkaRecord, kafkaNREventAllowedId))
                .flatMap(eventId -> nrEventClassMap.getEventClass(eventId)
                        .map(eventClass -> new EventClassAndKafkaRecord(eventClass.eventClass(), kafkaRecord)).stream());
    }

    private static void observeKafkaRecord(String s, ConsumerRecord<String, byte[]> kafkaRecord, Counter counter) {
        counter.increment();
        log.debug("{} {} {} {} {}", s, kafkaRecord.key(), kafkaRecord.headers(), kafkaRecord.offset(), kafkaRecord.partition());
    }

    private Optional<Integer> getEventId(Headers headers) {
        return StreamSupport.stream(headers.spliterator(), false)
                .filter(header -> EVENT_ID_HEADER_KEY.equals(header.key()))
                .flatMap(header -> Optional.ofNullable(header.value()).stream())
                .map(headerValue -> new String(headerValue, StandardCharsets.UTF_8))
                .map(Integer::parseInt)
                .findFirst();
    }

    @Override
    public Map<String, Optional<Set<String>>> getHeaderValueSetCheckMap() {
        return HEADER_MAP;
    }

}
