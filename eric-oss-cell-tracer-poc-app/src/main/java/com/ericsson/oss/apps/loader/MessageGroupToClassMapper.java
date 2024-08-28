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
package com.ericsson.oss.apps.loader;

import com.ericsson.pm_event.CuCpFunctionPmEventOuterClass;
import com.ericsson.pm_event.CuUpFunctionPmEventOuterClass;
import com.ericsson.pm_event.DuFunctionPmEventOuterClass;
import com.ericsson.pm_event.PmEventOuterClass;

public class MessageGroupToClassMapper {
    private MessageGroupToClassMapper() {
    }

    public static MessageGroupInfo getGroupInfo(PmEventOuterClass.PmEventMessageGroup pmEventMessageGroup) throws UnsupportedOperationException {
        return switch (pmEventMessageGroup) {
            case PM_EVENT_MESSAGE_GROUP_CUUP ->
                    new MessageGroupInfo(CuUpFunctionPmEventOuterClass.CuUpFunctionPmEvent.getDescriptor(), CuUpFunctionPmEventOuterClass.CuUpFunctionPmEvent.newBuilder(), CuUpFunctionPmEventOuterClass.CuUpFunctionPmEvent.class);
            case PM_EVENT_MESSAGE_GROUP_CUCP ->
                    new MessageGroupInfo(CuCpFunctionPmEventOuterClass.CuCpFunctionPmEvent.getDescriptor(), CuCpFunctionPmEventOuterClass.CuCpFunctionPmEvent.newBuilder(), CuCpFunctionPmEventOuterClass.CuCpFunctionPmEvent.class);
            case PM_EVENT_MESSAGE_GROUP_DU ->
                    new MessageGroupInfo(DuFunctionPmEventOuterClass.DuFunctionPmEvent.getDescriptor(), DuFunctionPmEventOuterClass.DuFunctionPmEvent.newBuilder(), DuFunctionPmEventOuterClass.DuFunctionPmEvent.class);
            default ->
                    throw new UnsupportedOperationException("cannot resolve group class for %s".formatted(pmEventMessageGroup));
        };
    }

}
