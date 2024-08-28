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

import com.ericsson.oss.apps.model.schema.EventClassSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class NREventClassLoaderTest extends AbstractConfigLoaderTest {

    private static final String RESOURCE_FILE_NAME = "nr_event_id_class.csv";
    private static final String RESOURCE_FILE_PATH = RESOURCE_FOLDER + RESOURCE_FILE_NAME;

    @Spy
    NREventClassMap nrEventClassMap;

    private NREventClassLoader nrEventClassLoader;

    @BeforeEach
    void setup() throws IOException {
        nrEventClassLoader = new NREventClassLoader(EventClassSchema.class, bdrClient, fileTracker, nrEventClassMap::putEventClass);
        InputStream inputStream = new ClassPathResource(RESOURCE_FILE_NAME).getInputStream();
        Mockito.when(bdrClient.getObject(RESOURCE_FILE_PATH)).thenReturn(inputStream);
        Mockito.when(bdrClient.getCheckSum(RESOURCE_FILE_PATH)).thenReturn(Optional.of(E_TAG));
    }

    @Test
    void loadIdentityEntry() {
        long numRecordsProcessed = nrEventClassLoader.loadData(RESOURCE_FOLDER);
        assertTrue(fileTracker.isFileLoaded(RESOURCE_FILE_PATH, E_TAG), "Expected ETAG not present in Map");
        assertEquals(5, numRecordsProcessed, "Expected number of records not loaded");
        Mockito.verify(nrEventClassMap, Mockito.times(3)).putEventClass(any());
    }

    @Test
    void checkSameIdentityEntryLoad() {
        fileTracker.clearTracker();
        this.loadIdentityEntry();
        long numRecordsProcessed = nrEventClassLoader.loadData(RESOURCE_FOLDER);
        assertEquals(0, numRecordsProcessed, "Expected number of records not loaded");
    }


}
