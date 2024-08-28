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
package com.ericsson.oss.apps.config;

import com.ericsson.oss.apps.proto.asn1.Asn1DecoderServiceOuterClass;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomMetricsConfiguration {

    public static final String APP = "app";
    public static final String ERIC_OSS_CELL_TRACER_POC_APP = "eric.oss.cell-tracer.poc.app";

    private static final String KAFKA_NR_EVENT = "kafka.nr.event.";

    private static final String DECODING_ASN1_NR = "decoding.nr.asn1.";
    public static final String INVALID_FORMAT = "invalid.format.";


    @Autowired
    private MeterRegistry meterRegistry;

    @Bean
    public Counter kafkaNREventBatches() {
        return meterRegistry.counter(KAFKA_NR_EVENT + "batches", APP, ERIC_OSS_CELL_TRACER_POC_APP);
    }

    @Bean
    public Counter kafkaNREventValidHeader() {
        return meterRegistry.counter(KAFKA_NR_EVENT + "valid.header", APP, ERIC_OSS_CELL_TRACER_POC_APP);
    }

    @Bean
    public Counter kafkaNREventValidFormat() {
        return meterRegistry.counter(KAFKA_NR_EVENT + "valid.format", APP, ERIC_OSS_CELL_TRACER_POC_APP);
    }

    @Bean
    public Counter kafkaNREventValidId() {
        return meterRegistry.counter(KAFKA_NR_EVENT + "valid.id", APP, ERIC_OSS_CELL_TRACER_POC_APP);
    }

    @Bean
    public Counter kafkaNREventAllowedId() {
        return meterRegistry.counter(KAFKA_NR_EVENT + "allowed.id", APP, ERIC_OSS_CELL_TRACER_POC_APP);
    }

    @Bean
    public Counter kafkaNREventValidNode() {
        return meterRegistry.counter(KAFKA_NR_EVENT + "valid.node", APP, ERIC_OSS_CELL_TRACER_POC_APP);
    }


    @Bean
    public Counter kafkaNREventInvalidFormat() {
        return meterRegistry.counter(DECODING_ASN1_NR + "invalid.format", APP, ERIC_OSS_CELL_TRACER_POC_APP);
    }

    @Bean
    public Counter decodingNRAsn1ValidFormat() {
        return meterRegistry.counter(DECODING_ASN1_NR + "valid.format", APP, ERIC_OSS_CELL_TRACER_POC_APP);
    }

    @Bean
    public Counter decodingNRAsn1InvalidFormatGpbDecoderException() {
        return meterRegistry.counter(DECODING_ASN1_NR + INVALID_FORMAT + Asn1DecoderServiceOuterClass.NRAsn1ParsingStatus.GPB_DECODER_EXCEPTION, APP, ERIC_OSS_CELL_TRACER_POC_APP);
    }

    @Bean
    public Counter decodingNRAsn1InvalidFormatIoException() {
        return meterRegistry.counter(DECODING_ASN1_NR + INVALID_FORMAT + Asn1DecoderServiceOuterClass.NRAsn1ParsingStatus.IO_EXCEPTION, APP, ERIC_OSS_CELL_TRACER_POC_APP);
    }

    @Bean
    public Counter decodingNRAsn1InvalidFormatInternalError() {
        return meterRegistry.counter(DECODING_ASN1_NR + INVALID_FORMAT + Asn1DecoderServiceOuterClass.NRAsn1ParsingStatus.INTERNAL_ERROR, APP, ERIC_OSS_CELL_TRACER_POC_APP);
    }
    @Bean
    public Counter decodingNRAsn1InvalidFormatUnrecognized() {
        return meterRegistry.counter(DECODING_ASN1_NR + INVALID_FORMAT + Asn1DecoderServiceOuterClass.NRAsn1ParsingStatus.UNRECOGNIZED, APP, ERIC_OSS_CELL_TRACER_POC_APP);
    }



}
