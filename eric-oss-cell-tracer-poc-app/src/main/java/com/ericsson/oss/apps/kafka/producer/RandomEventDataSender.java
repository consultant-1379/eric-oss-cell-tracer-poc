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
package com.ericsson.oss.apps.kafka.producer;

import com.ericsson.oss.apps.generator.RandomEventGenerator;
import com.ericsson.pm_event.PmEventOuterClass;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.ericsson.oss.apps.kafka.consumer.NREventDataRetrieval.EVENT_ID_HEADER_KEY;

@RequiredArgsConstructor
public class RandomEventDataSender {
    private final KafkaTemplate<String, byte[]> nrEventTemplate;
    private final RandomEventGenerator generator;
    private final String topic;

    @Getter
    PmEventOuterClass.PmEvent lastSentPMEvent;

    @Scheduled(cron = "${app.generator.events.rate}")
    public void sendNREvent() {
        PmEventOuterClass.PmEvent pmEvent = generator.nextObject(PmEventOuterClass.PmEvent.class);

        List<Header> headers = List.of(new RecordHeader(EVENT_ID_HEADER_KEY,
                Long.toString(pmEvent.getEventId()).getBytes(StandardCharsets.UTF_8)));

        ProducerRecord<String, byte[]> eventRecord = new ProducerRecord<>(topic, null,
                pmEvent.getHeader().getNetworkManagedElement(), pmEvent.toByteArray(), headers);
        nrEventTemplate.send(eventRecord);
        lastSentPMEvent = pmEvent;
    }

}
