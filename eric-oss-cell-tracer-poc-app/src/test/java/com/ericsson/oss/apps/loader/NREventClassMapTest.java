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

import com.ericsson.oss.apps.api.model.controller.NetworkElement;
import com.ericsson.oss.apps.api.model.controller.TrcLevel;
import com.ericsson.oss.apps.model.NREventClass;
import com.ericsson.oss.apps.model.subscription.Subscription;
import com.google.protobuf.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.ericsson.pm_event.PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_CUUP;
import static com.ericsson.pm_event.PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_DU;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NREventClassMapTest {

    NREventClassMap nrEventClassMap;
    List<NREventClass> nrEventClassList = List.of(
            new NREventClass(1L, Message.class, TrcLevel.CELL, PM_EVENT_MESSAGE_GROUP_DU, NetworkElement.RC, NetworkElement.GNODEB, null),
            new NREventClass(2L, Message.class, TrcLevel.CELL, PM_EVENT_MESSAGE_GROUP_CUUP, NetworkElement.RC, NetworkElement.GNODEB, null),
            new NREventClass(3L, Message.class, TrcLevel.UE, PM_EVENT_MESSAGE_GROUP_DU, NetworkElement.RC, NetworkElement.GNODEB, null),
            new NREventClass(4L, Message.class, TrcLevel.UE, PM_EVENT_MESSAGE_GROUP_CUUP, NetworkElement.RC, NetworkElement.GNODEB, null)
    );

    @BeforeEach
    void setup() {
        nrEventClassMap = new NREventClassMap();
        nrEventClassList.forEach(nrEventClassMap::putEventClass);
    }

    @Test
    void filtersToEIds() {
        var subscription = new Subscription();
        subscription.setEventIdList(List.of());
        subscription.setTrcLevelList(List.of(TrcLevel.UE));
        subscription.setEventMessageGroupList(List.of(PM_EVENT_MESSAGE_GROUP_CUUP));
        assertEquals(Optional.of(List.of(nrEventClassList.get(3).eventId())), nrEventClassMap.filtersToEIds(subscription));
    }

    @Test
    void trcLevelToEIdsNullEidsNullTrcLevel() {
        assertEquals(Optional.empty(), nrEventClassMap.trcLevelToEIds(List.of(), List.of()));
    }

    @Test
    void trcLevelToEIdsNullEids() {
        assertEquals(Optional.of(List.of(nrEventClassList.get(2).eventId(), nrEventClassList.get(3).eventId())),
                nrEventClassMap.trcLevelToEIds(List.of(), List.of(TrcLevel.UE)));
    }

    @Test
    void trcLevelToEIdsNullTrcLevel() {
        assertEquals(Optional.of(List.of(nrEventClassList.get(0).eventId(), nrEventClassList.get(3).eventId())),
                nrEventClassMap.trcLevelToEIds(List.of(1L, 4L), List.of()));
    }

    @Test
    void trcLevelToEIds() {
        assertEquals(Optional.of(List.of(nrEventClassList.get(3).eventId())),
                nrEventClassMap.trcLevelToEIds(List.of(1L, 4L), List.of(TrcLevel.UE)));
    }

    @Test
    void getEventClass() {
        assertEquals(Optional.of(nrEventClassList.get(0)), nrEventClassMap.getEventClass(1L));
    }

    @Test
    void getEventClasses() {
        assertEquals(4, nrEventClassMap.getEventClasses().size());
    }
}