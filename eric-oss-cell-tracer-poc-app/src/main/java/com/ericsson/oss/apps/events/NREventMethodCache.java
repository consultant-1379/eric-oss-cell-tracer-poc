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

import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
@Component
public class NREventMethodCache {

    @Cacheable(value = "getPayloadSetterFromCache", cacheManager = "cacheManager")
    public Method getPayloadSetterFromCache(Class<? extends Message.Builder> messageGroupBuilderClass, Class<? extends Message> messageClass) throws NoSuchMethodException {
        return messageGroupBuilderClass.getMethod("set" + messageClass.getSimpleName(), messageClass);
    }

    @Cacheable(value = "getPayloadGetterFromCache", cacheManager = "cacheManager")
    public Method getPayloadGetterFromCache(Class<? extends Message> messageGroupClass, Class<? extends Message> messageClass) throws NoSuchMethodException {
        return messageGroupClass.getMethod("get" + messageClass.getSimpleName());
    }

    @Cacheable(value = "getParseGroupFromCache", cacheManager = "cacheManager")
    public Method getParseGroupFrom(Class<?> messageGroupClass) throws NoSuchMethodException {
        return messageGroupClass.getMethod("parseFrom", com.google.protobuf.ByteString.class);
    }

    @Cacheable(value = "getNciCache", cacheManager = "cacheManager")
    public Optional<Method> getNci(Class<?> eventClass) {
        try {
            return Optional.of(eventClass.getMethod("getNci"));
        } catch (NoSuchMethodException e) {
            log.debug("No NCI field found, probably a NrNodeEvent", e);
            return Optional.empty();
        }
    }

    @Cacheable(value = "getMainPlmnIdCache", cacheManager = "cacheManager")
    public Method getMainPlmnId(Class<?> eventClass) throws NoSuchMethodException {
        return eventClass.getMethod("getMainPlmnId");
    }

    @Cacheable(value = "getGnbIdCache", cacheManager = "cacheManager")
    public Method getGnbId(Class<?> eventClass) throws NoSuchMethodException {
        return eventClass.getMethod("getGnbId");
    }

    @Cacheable(value = "getGnbIdLengthCache", cacheManager = "cacheManager")
    public Method getGnbIdLength(Class<?> eventClass) throws NoSuchMethodException {
        return eventClass.getMethod("getGnbIdLength");
    }
}
