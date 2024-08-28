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

import com.ericsson.oss.apps.model.schema.NodeIdentitySchema;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
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
class AllowedNodesLoaderTest extends AbstractConfigLoaderTest {


    private static final String RESOURCE_FILE_NAME = "allowed_nodes.csv";
    private static final String RESOURCE_FILE_PATH = RESOURCE_FOLDER + RESOURCE_FILE_NAME;
    private final static String FDN = "SubNetwork=Athlone,MeContext=MA2B0001A2,ManagedElement=MA2B0001A2,GNBDUFunction=1,NRCellDU=JA2B0001A11";

    @Spy
    AllowedNodeMap allowedNodeMap;

    private AllowedNodeLoader identityEntryLoader;

    @BeforeEach
    void setup() throws IOException {
        identityEntryLoader = new AllowedNodeLoader(NodeIdentitySchema.class, bdrClient, fileTracker, allowedNodeMap::addNode);
        InputStream inputStream = new ClassPathResource(RESOURCE_FILE_NAME).getInputStream();
        Mockito.when(bdrClient.getObject(RESOURCE_FILE_PATH)).thenReturn(inputStream);
        Mockito.when(bdrClient.getCheckSum(RESOURCE_FILE_PATH)).thenReturn(Optional.of(E_TAG));
    }

    @Test
    void loadIdentityEntry() {
        long numRecordsProcessed = identityEntryLoader.loadData(RESOURCE_FOLDER);
        assertTrue(fileTracker.isFileLoaded(RESOURCE_FILE_PATH, E_TAG), "Expected ETAG not present in Map");
        assertEquals(20, numRecordsProcessed, "Expected number of records not loaded");
        Mockito.verify(allowedNodeMap, Mockito.times(20)).addNode(any());
    }

    @Test
    void checkSameIdentityEntryLoad() {
        fileTracker.clearTracker();
        this.loadIdentityEntry();
        long numRecordsProcessed = identityEntryLoader.loadData(RESOURCE_FOLDER);
        assertEquals(0, numRecordsProcessed, "Expected number of records not loaded");
    }

    @Test
    void transformDataValidInput() {
        this.loadIdentityEntry();
        allowedNodeMap.isAllowed(ManagedObjectId.of(FDN));
    }

}
