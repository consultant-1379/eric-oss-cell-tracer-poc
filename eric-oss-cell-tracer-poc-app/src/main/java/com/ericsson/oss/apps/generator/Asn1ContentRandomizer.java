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
package com.ericsson.oss.apps.generator;

import com.ericsson.oss.apps.model.NREventClass;
import com.ericsson.pm_event.PmEventOuterClass;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.api.ContextAwareRandomizer;
import org.jeasy.random.api.RandomizerContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class Asn1ContentRandomizer implements ContextAwareRandomizer<Integer> {
    private static final Map<ClassField, Optional<Field>> METHODS = new ConcurrentHashMap<>();

    private RandomizerContext context;

    private final Map<Class<? extends Message>, PmEventOuterClass.PmEventMessageGroup> classMapToMsgGroup;

    private static final ByteString asn1ContentBytes = ByteString.copyFrom(new byte[]{0, 15, 64, 72, 0, 0, 5, 0, 85, 0, 2, 0, 6, 0, 38, 0, 24, 23, 126, 0, 65, 121, 0, 13, 1, 98, -14, 8, -16, -1, 0, 0, 48, 16, 2, 1, 32, 46, 2, -16, -16, 0, 121, 0, 15, 64, 98, -14, 8, 0, 11, 60, 0, 16, 98, -14, 8, 0, 0, 5, 0, 90, 64, 1, 24, 0, 26, 0, 7, 0, 0, 0, 0, 0, 0, 0});
    private static final List<Integer> protocolNameValue = List.of(17);
    private static final String PROTOCOL_NAME_FIELD_NAME = "protocolName_";
    private static final String MSG_CONTENT_FIELD_NAME = "msgContent_";

    public Asn1ContentRandomizer(List<NREventClass> nrEventClassList) {
        this.classMapToMsgGroup = nrEventClassList.stream().collect(Collectors.toMap(NREventClass::eventClass, NREventClass::pmEventMessageGroup));
    }

    @Override
    public Integer getRandomValue() {
        // look up the message class, check if it's in the right group
        // if it's there, set the other asn.1 content values
        if (context.getRootObject() instanceof Message rootObject &&
                PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_CUCP.equals(classMapToMsgGroup.get(rootObject.getClass()))) {
            setFieldInCurrentObject(PROTOCOL_NAME_FIELD_NAME, protocolNameValue);
            setFieldInCurrentObject(MSG_CONTENT_FIELD_NAME, asn1ContentBytes);
        }
        return 1;
    }

    private void setFieldInCurrentObject(String fieldName, Object fieldValue) {
        METHODS.computeIfAbsent(new ClassField(context.getCurrentObject().getClass(), fieldName), asn1Class -> getOptionalField(asn1Class.messageFieldClass, fieldName))
                .ifPresent(field -> setFieldContent(field, fieldValue));
    }

    private record ClassField(Class<?> messageFieldClass, String fieldName) {
    }

    private void setFieldContent(Field field, Object content) {
        Object entity = context.getCurrentObject();
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, entity, content);
    }

    private Optional<Field> getOptionalField(Class<?> getTargetType, String fieldName) {
        return Optional.ofNullable(ReflectionUtils.findField(getTargetType, fieldName));
    }

    @Override
    public void setRandomizerContext(RandomizerContext randomizerContext) {
        this.context = randomizerContext;
    }
}
