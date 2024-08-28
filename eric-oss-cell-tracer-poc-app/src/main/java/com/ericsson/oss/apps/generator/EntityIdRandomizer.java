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

import com.ericsson.oss.apps.model.AllowedNode;
import lombok.extern.slf4j.Slf4j;
import org.jeasy.random.api.ContextAwareRandomizer;
import org.jeasy.random.api.RandomizerContext;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class EntityIdRandomizer implements ContextAwareRandomizer<Long> {
    static final int GNB_ID_LENGTH = 22;
    private static final SecureRandom GENERATOR = new SecureRandom();
    private static final Map<Class<?>, Optional<Field>> METHODS = new ConcurrentHashMap<>();

    private final AtomicReference<AllowedNode> entityHolder;

    private RandomizerContext context;

    EntityIdRandomizer(AtomicReference<AllowedNode> entityHolder) {
        this.entityHolder = entityHolder;
    }

    @Override
    public Long getRandomValue() {
        long gnbId = Optional.ofNullable(entityHolder.get())
                .flatMap(AllowedNode::gnbId)
                .orElseGet(() -> GENERATOR.nextLong(40) + 1);

        METHODS.computeIfAbsent(context.getCurrentObject().getClass(), this::getOptionalField)
                .ifPresent(field -> setNci(field, gnbId));

        return gnbId;
    }

    private Optional<Field> getOptionalField(Class<?> getTargetType) {
        return Optional.ofNullable(ReflectionUtils.findField(getTargetType, "nci_"));
    }

    private void setNci(Field field, Long gnbId) {
        long cellLocalID = 3 * gnbId + GENERATOR.nextLong(3) - 2;
        Object entity = context.getCurrentObject();
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, entity, (long) (gnbId * Math.pow(2, (36-GNB_ID_LENGTH)) + cellLocalID));
    }

    @Override
    public void setRandomizerContext(RandomizerContext randomizerContext) {
        this.context = randomizerContext;
    }
}
