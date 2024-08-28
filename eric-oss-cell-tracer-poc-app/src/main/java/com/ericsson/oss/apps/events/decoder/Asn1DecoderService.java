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

import java.util.List;
import java.util.stream.IntStream;

public interface Asn1DecoderService {
    default List<String> decode(List<byte[]> events) {
        return IntStream.range(0, events.size()).mapToObj(idx -> "").toList();
    }
}
