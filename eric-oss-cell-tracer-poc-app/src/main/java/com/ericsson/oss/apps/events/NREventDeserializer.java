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

import com.ericsson.oss.apps.kafka.consumer.EventClassAndKafkaRecord;
import com.ericsson.oss.apps.loader.MessageGroupInfo;
import com.ericsson.oss.apps.loader.MessageGroupToClassMapper;
import com.ericsson.oss.apps.model.embeddables.EventId;
import com.ericsson.pm_event.PmEventOuterClass;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import io.micrometer.core.instrument.Counter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Deserializes events
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class NREventDeserializer {

    private final NREventMethodCache nrEventMethodCache;

    private final Counter kafkaNREventValidFormat;
    private final Counter kafkaNREventInvalidFormat;

    public Optional<DeserializedNREventRecord> deserialize(EventClassAndKafkaRecord eventClassAndKafkaRecord) {
        if (eventClassAndKafkaRecord == null) {
            return Optional.empty();
        }
        ConsumerRecord<String, byte[]> consumerRecord = eventClassAndKafkaRecord.kafkaRecord();
        try {
            PmEventOuterClass.PmEvent pmEvent = PmEventOuterClass.PmEvent.parseFrom(consumerRecord.value());
            Class<? extends Message> payloadType = eventClassAndKafkaRecord.eventClass();
            MessageGroupInfo groupInfo = MessageGroupToClassMapper.getGroupInfo(pmEvent.getGroup());

            Method parseGroupMethod = nrEventMethodCache.getParseGroupFrom(groupInfo.groupClass());
            Message decodedGroupPayload = (Message) parseGroupMethod.invoke(null, pmEvent.getPayload());
            Method payloadGetter = nrEventMethodCache.getPayloadGetterFromCache(groupInfo.groupClass(), payloadType);
            Message decodedPayload = (Message) payloadGetter.invoke(decodedGroupPayload);
            kafkaNREventValidFormat.increment();
            return Optional.of(new DeserializedNREventRecord(
                    new EventId(consumerRecord.offset(), consumerRecord.partition()),
                    pmEvent,
                    decodedPayload,
                    payloadType,
                    consumerRecord.value()));
        } catch (InvalidProtocolBufferException | SerializationException | UnsupportedOperationException |
                 NoSuchMethodException |
                 InvocationTargetException | IllegalAccessException e) {
            kafkaNREventInvalidFormat.increment();
            log.error("Cannot parse proto for topic: {} offset: {} key : {} ", consumerRecord.topic(), consumerRecord.offset(), consumerRecord.key(), e);
        }
        return Optional.empty();
    }
}
