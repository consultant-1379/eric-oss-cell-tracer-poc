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

import com.ericsson.oss.apps.generator.RandomEventGenerator;
import com.ericsson.oss.apps.proto.asn1.Asn1DecoderServiceOuterClass;
import com.ericsson.pm_event.PmEventOuterClass;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Base64;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.ericsson.oss.apps.service.decoder.asn1.nr.NRAsn1DecoderResourceAbstractTest.BASE64_EVENT_3031;
import static com.ericsson.oss.apps.service.decoder.asn1.nr.NRAsn1DecoderResourceAbstractTest.DECODED_ASN1;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@Testcontainers
@SpringBootTest(properties = {"app.asn1.decoder.enabled=true", "app.generator.events.enabled=true", "app.data.config=classpath:"})
@Slf4j
class Asn1DecoderServiceITTest {

    @Autowired
    Asn1DecoderService asn1DecoderService;
    @MockBean
    Asn1DecoderCounters asn1DecoderCounters;
    @Autowired
    RandomEventGenerator randomEventGenerator;

    // This variable is duplicated with the one in maven and should be kept in line.
    // If removed, this test wil only work in maven.
    private static final String asn1DecoderVersion = System.getProperty("version.asn1-decoder", "1.8.0-1");
    private static final String asn1ImageTag = "eric-oss-asn1-decoder-poc:%s".formatted(asn1DecoderVersion);
    private static final DockerImageName asn1DecoderDockerImageName = DockerImageName.parse(asn1ImageTag)
            .withRegistry("armdocker.rnd.ericsson.se/proj-eric-oss-drop");

    @Container
    private static final GenericContainer<?> grpcContainer = new GenericContainer<>(asn1DecoderDockerImageName).withExposedPorts(8080);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("grpc.client.asn1Decoder.address", () -> "static://%s:%d".formatted(grpcContainer.getHost(), grpcContainer.getMappedPort(8080)));
    }

    @Test
    void testAsn1Decoding() {
        assertEquals(DECODED_ASN1, asn1DecoderService.decode(List.of(Base64.getDecoder().decode(BASE64_EVENT_3031))).get(0));
        verify(asn1DecoderCounters, times(1)).incrementAsn1Counter(Asn1DecoderServiceOuterClass.NRAsn1ParsingStatus.OK);
    }

    @Test
    void testAsn1Generation() {
        var parsed = IntStream.range(0, 10).mapToObj(idx -> {
                    PmEventOuterClass.PmEvent pmEvent = randomEventGenerator.nextObject(PmEventOuterClass.PmEvent.class);
                    return asn1DecoderService.decode(List.of(pmEvent.toByteArray())).stream();
                })
                .flatMap(Function.identity())
                .filter(decoded -> !decoded.isEmpty())
                .count();
        assertEquals(1, parsed);
    }

}