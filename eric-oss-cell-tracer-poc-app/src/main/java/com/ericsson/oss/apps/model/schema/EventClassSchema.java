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
package com.ericsson.oss.apps.model.schema;

import com.ericsson.oss.apps.api.model.controller.MessageDirection;
import com.ericsson.oss.apps.api.model.controller.NetworkElement;
import com.ericsson.oss.apps.api.model.controller.TrcLevel;
import com.ericsson.pm_event.PmEventOuterClass;
import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class EventClassSchema {

    @CsvBindByName
    private Long eventId;

    @CsvBindByName
    private String className;

    @CsvBindByName
    private TrcLevel traceLevel;

    @CsvBindByName(column = "nwFunction")
    private PmEventOuterClass.PmEventMessageGroup pmEventMessageGroup;

    @CsvBindByName
    private NetworkElement networkElement1;

    @CsvBindByName
    private NetworkElement networkElement2;

    @CsvBindByName
    private Integer messageDirection;

    public MessageDirection getMessageDirection() {
        if (Integer.valueOf(1).equals(messageDirection)) {
            return MessageDirection.OUTGOING;
        } else if (Integer.valueOf(2).equals(messageDirection)) {
            return MessageDirection.INCOMING;
        }
        return MessageDirection.UNKNOWN;
    }
}
