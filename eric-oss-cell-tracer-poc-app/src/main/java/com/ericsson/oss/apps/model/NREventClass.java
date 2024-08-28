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

/*******************************************************************************
 * COPYRIGHT Ericsson 2021
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
package com.ericsson.oss.apps.model;

import com.ericsson.oss.apps.api.model.controller.MessageDirection;
import com.ericsson.oss.apps.api.model.controller.NetworkElement;
import com.ericsson.oss.apps.api.model.controller.TrcLevel;
import com.ericsson.pm_event.PmEventOuterClass;
import com.google.protobuf.Message;

public record NREventClass(long eventId, Class<? extends Message> eventClass, TrcLevel traceLevel,
                           PmEventOuterClass.PmEventMessageGroup pmEventMessageGroup,
                           NetworkElement networkElement1, NetworkElement networkElement2,
                           MessageDirection messageDirection) {
    public String getEventClassName() {
        return eventClass.getSimpleName();
    }
}
