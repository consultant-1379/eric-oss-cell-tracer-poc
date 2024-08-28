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

import com.ericsson.oss.apps.model.embeddables.EventId;
import com.ericsson.pm_event.PmEventOuterClass;
import com.google.protobuf.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public class DeserializedNREventRecord {
    private final EventId id;
    private final PmEventOuterClass.PmEvent message;
    private final Message messagePayload;
    private final Class<?> payloadType;
    private final byte[] messageBytes;
}
