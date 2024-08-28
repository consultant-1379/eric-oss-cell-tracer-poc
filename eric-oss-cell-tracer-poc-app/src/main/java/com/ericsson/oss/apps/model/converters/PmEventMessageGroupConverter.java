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

import com.ericsson.pm_event.PmEventOuterClass;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class PmEventMessageGroupConverter implements AttributeConverter<PmEventOuterClass.PmEventMessageGroup, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PmEventOuterClass.PmEventMessageGroup messageGroup) {
        if (messageGroup == null) {
            return null;
        }
        if (messageGroup == PmEventOuterClass.PmEventMessageGroup.UNRECOGNIZED) {
            return -1;
        }
        return messageGroup.getNumber();
    }

    @Override
    public PmEventOuterClass.PmEventMessageGroup convertToEntityAttribute(Integer code) {
        if (code == null) {
            return null;
        }
        if (code == -1) {
            return PmEventOuterClass.PmEventMessageGroup.UNRECOGNIZED;
        }

        return PmEventOuterClass.PmEventMessageGroup.forNumber(code);
    }

}
