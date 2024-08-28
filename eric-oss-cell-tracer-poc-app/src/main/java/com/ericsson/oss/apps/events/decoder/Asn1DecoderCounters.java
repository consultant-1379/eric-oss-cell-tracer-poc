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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Counters packed here to simplify autowired constructor and config class.
 */
@RequiredArgsConstructor
@Component
public class Asn1DecoderCounters {
    private final Counter decodingNRAsn1ValidFormat;
    private final Counter decodingNRAsn1InvalidFormatGpbDecoderException;
    private final Counter decodingNRAsn1InvalidFormatIoException;
    private final Counter decodingNRAsn1InvalidFormatInternalError;
    private final Counter decodingNRAsn1InvalidFormatUnrecognized;

    void incrementAsn1Counter(Asn1DecoderServiceOuterClass.NRAsn1ParsingStatus nrAsn1ParsingStatus) {
        switch (nrAsn1ParsingStatus) {
            case OK -> decodingNRAsn1ValidFormat.increment();
            case GPB_DECODER_EXCEPTION -> decodingNRAsn1InvalidFormatGpbDecoderException.increment();
            case IO_EXCEPTION -> decodingNRAsn1InvalidFormatIoException.increment();
            case INTERNAL_ERROR -> decodingNRAsn1InvalidFormatInternalError.increment();
            case UNRECOGNIZED -> decodingNRAsn1InvalidFormatUnrecognized.increment();
        }
    }
}
