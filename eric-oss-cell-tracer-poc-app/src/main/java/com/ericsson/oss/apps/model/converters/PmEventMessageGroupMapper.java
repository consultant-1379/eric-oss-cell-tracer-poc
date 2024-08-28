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
package com.ericsson.oss.apps.model.converters;

import com.ericsson.oss.apps.api.model.controller.NwFunction;
import com.ericsson.pm_event.PmEventOuterClass;

public class PmEventMessageGroupMapper {

    private PmEventMessageGroupMapper() {
    }

    public static PmEventOuterClass.PmEventMessageGroup nwFunctionDtoToEntity(NwFunction nwFunction) {
        return switch (nwFunction) {
            case CUUP -> PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_CUUP;
            case CUCP -> PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_CUCP;
            case DU -> PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_DU;
            case NO_VALUE -> PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_NO_VALUE;
            case UNRECOGNIZED -> PmEventOuterClass.PmEventMessageGroup.UNRECOGNIZED;
        };
    }

    public static NwFunction nwFunctionEntityToDto(PmEventOuterClass.PmEventMessageGroup pmEventMessageGroup) throws UnsupportedOperationException {
        return switch (pmEventMessageGroup) {
            case PM_EVENT_MESSAGE_GROUP_CUUP -> NwFunction.CUUP;
            case PM_EVENT_MESSAGE_GROUP_CUCP -> NwFunction.CUCP;
            case PM_EVENT_MESSAGE_GROUP_DU -> NwFunction.DU;
            case PM_EVENT_MESSAGE_GROUP_NO_VALUE -> NwFunction.NO_VALUE;
            default -> NwFunction.UNRECOGNIZED;
        };
    }
}
