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

import com.ericsson.oss.apps.bdr.client.BdrClient;
import com.ericsson.oss.apps.bdr.loader.CsvLoader;
import com.ericsson.oss.apps.bdr.loader.FileTracker;
import com.ericsson.oss.apps.model.NREventClass;
import com.ericsson.oss.apps.model.schema.EventClassSchema;
import com.google.protobuf.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
public class NREventClassLoader extends CsvLoader<EventClassSchema, NREventClass> {

    public static final String PATH_TEMPLATE = "%snr_event_id_class.csv";

    public NREventClassLoader(
            Class<EventClassSchema> clazz,
            BdrClient bdrClient,
            FileTracker fileTracker,
            Consumer<NREventClass> recordConsumer
    ) {
        super(clazz, PATH_TEMPLATE, bdrClient, fileTracker, recordConsumer);
    }


    @Override
    protected InputStream interceptObjectInputStream(InputStream inputStream) {
        return inputStream;
    }

    @Override
    protected Optional<NREventClass> transformData(EventClassSchema eventClassSchema) {

        if (StringUtils.isEmpty(eventClassSchema.getClassName())) {
            log.warn("event to class mapping not defined properly in {}", eventClassSchema);
            return Optional.empty();
        }
        if (eventClassSchema.getEventId() == null) {
            log.error("Null field found for event id {} ", eventClassSchema);
            return Optional.empty();
        }
        try {
            NREventClass nrEventClass = new NREventClass(eventClassSchema.getEventId(),
                    Class.forName(eventClassSchema.getClassName()).asSubclass(Message.class),
                    eventClassSchema.getTraceLevel(),
                    eventClassSchema.getPmEventMessageGroup(),
                    eventClassSchema.getNetworkElement1(), eventClassSchema.getNetworkElement2(),
                    eventClassSchema.getMessageDirection());
            return Optional.of(nrEventClass);
        } catch (ClassNotFoundException e) {
            log.error("Cannot find class {} for event id {} ", eventClassSchema.getClassName(), eventClassSchema.getEventId(), e);
            return Optional.empty();
        }
    }

}
