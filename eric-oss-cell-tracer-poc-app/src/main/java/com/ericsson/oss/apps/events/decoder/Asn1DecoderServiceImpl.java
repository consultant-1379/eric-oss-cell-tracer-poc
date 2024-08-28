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

import com.ericsson.oss.apps.proto.asn1.Asn1DecoderServiceGrpc;
import com.ericsson.oss.apps.proto.asn1.Asn1DecoderServiceOuterClass;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;

import java.util.List;

/**
 * This is a naive (blocking) implementation.
 */
@RequiredArgsConstructor
@Slf4j
public class Asn1DecoderServiceImpl implements Asn1DecoderService {
    private final Asn1DecoderServiceGrpc.Asn1DecoderServiceBlockingStub asn1DecoderServiceStub;
    private final Asn1DecoderCounters asn1DecoderCounters;

    @Override
    @Retryable(maxAttemptsExpression = "${app.asn1.decoder.max-retries}", backoff = @Backoff(delayExpression = "${app.asn1.decoder.backoff.delay}", multiplierExpression = "${app.asn1.decoder.backoff.multiplier}"))
    public List<String> decode(List<byte[]> events) {
        var byteStringEventList = events.stream().map(com.google.protobuf.ByteString::copyFrom).toList();
        return asn1DecoderServiceStub.decodeNRAsn1Events(Asn1DecoderServiceOuterClass.NRAsn1EventRequest.newBuilder().addAllEncodedEvent(byteStringEventList).build())
                .getDecodedAsn1List().stream().peek(nrAsn1Decoded -> asn1DecoderCounters.incrementAsn1Counter(nrAsn1Decoded.getParsingStatus()))
                .map(Asn1DecoderServiceOuterClass.NRAsn1Decoded::getDecodedEvent).toList();
    }
}
