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
package com.ericsson.oss.apps.service;

import com.ericsson.oss.apps.config.DccConfig;
import com.ericsson.oss.apps.dcc.DccClient;
import com.ericsson.oss.apps.dcc.model.IDS;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DccServiceImplTest {

    public static final String SUB_NAME = "name";
    DccService dccService;

    @Mock
    DccClient dccClient;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        dccService = ((new DccConfig())).dccService(dccClient, objectMapper);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "true", "false"
    })
    void createIds(boolean dccResult) throws IOException {
        IDS ids = objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream("ids/ids.json"), IDS.class);
        when(dccClient.createIds(ids.getVersion(), ids.getSubscriptions())).thenReturn(dccResult);
        dccService.createIds();
        verify(dccClient, times(1)).createIds(ids.getVersion(), ids.getSubscriptions());
    }

    @ParameterizedTest
    @CsvSource(value = {
            "true", "false"
    })
    void deleteIds(boolean dccResult) {
        when(dccClient.deleteIds()).thenReturn(dccResult);
        dccService.deleteIds();
        verify(dccClient, times(1)).deleteIds();
    }

    @ParameterizedTest
    @CsvSource(value = {
            "true", "false"
    })
    void patchSubscription(boolean dccResult) {
        when(dccClient.patchIdsSubscription(SUB_NAME, Map.of("key", List.of("value")))).thenReturn(dccResult);
        assertEquals(dccResult, dccService.patchDccSubscription(SUB_NAME, Map.of("key", List.of("value"))));
    }

    @ParameterizedTest
    @CsvSource(value = {
            "true", "false"
    })
    void blankSubscription(boolean dccResult) {
        when(dccClient.blankIdsSubscription(SUB_NAME)).thenReturn(dccResult);
        assertEquals(dccResult, dccService.blankDccSubscription(SUB_NAME));
    }
}