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
package com.ericsson.oss.apps.events.decoder;

import com.ericsson.oss.apps.proto.asn1.Asn1DecoderServiceOuterClass;
import io.micrometer.core.instrument.Counter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ericsson.oss.apps.proto.asn1.Asn1DecoderServiceOuterClass.NRAsn1ParsingStatus.*;
import static org.mockito.Mockito.verify;

class Asn1DecoderCountersTest {

    Asn1DecoderCounters asn1DecoderCounters;
    Map<Asn1DecoderServiceOuterClass.NRAsn1ParsingStatus, Counter> counterMap;

    @BeforeEach
    void setUp() {
        counterMap = Arrays.stream(Asn1DecoderServiceOuterClass.NRAsn1ParsingStatus.values())
                .collect(Collectors.toMap(
                parsingStatus -> parsingStatus,
                parsingStatus -> Mockito.mock(Counter.class)));
        asn1DecoderCounters = new Asn1DecoderCounters(
                counterMap.get(OK),
                counterMap.get(GPB_DECODER_EXCEPTION),
                counterMap.get(IO_EXCEPTION),
                counterMap.get(INTERNAL_ERROR),
                counterMap.get(UNRECOGNIZED));
    }

    @Test
    void incrementAsn1Counter() {
        counterMap.forEach((idx, counter) -> {
            asn1DecoderCounters.incrementAsn1Counter(idx);
            verify(counterMap.get(idx)).increment();
        });
    }
}