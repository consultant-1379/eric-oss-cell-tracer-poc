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

import com.ericsson.oss.apps.events.decoder.Asn1DecoderCounters;
import com.ericsson.oss.apps.events.decoder.Asn1DecoderService;
import com.ericsson.oss.apps.events.decoder.Asn1DecoderServiceImpl;
import com.ericsson.oss.apps.proto.asn1.Asn1DecoderServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.asn1.decoder", name = "enabled", havingValue = "true")
public class GrpcServiceConfig {

    @GrpcClient("asn1Decoder")
    Asn1DecoderServiceGrpc.Asn1DecoderServiceBlockingStub asn1DecoderServiceBlockingStub;

    @Bean
    Asn1DecoderService asn1DecoderService(Asn1DecoderCounters asn1DecoderCounters) {
        return new Asn1DecoderServiceImpl(asn1DecoderServiceBlockingStub, asn1DecoderCounters);
    }
}