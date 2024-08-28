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
package com.ericsson.oss.apps.model.subscription;

import com.ericsson.oss.apps.api.model.controller.NwFunction;
import com.ericsson.oss.apps.api.model.controller.SubscriptionDto;
import com.ericsson.oss.apps.api.model.controller.TrcLevel;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.pm_event.PmEventOuterClass;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.ericsson.oss.apps.service.SubscriptionService.SUBSCRIPTION_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubscriptionTest {
    public static final String FDN = "SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR01gNodeBRadio00010,ManagedElement=NR01gNodeBRadio00010";

    @Test
    void mapSubscriptionDtoToEntity() {
        var subscriptionDto = getSubscriptionDto(FDN);
        var subscriptionEntity = new Subscription(subscriptionDto);
        assertEquals(subscriptionDto.geteId(), subscriptionEntity.getEventIdList());
        assertEquals(subscriptionDto.getTrcLevels(), subscriptionEntity.getTrcLevelList());
        assertEquals(List.of(PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_CUUP), subscriptionEntity.getEventMessageGroupList());
        assertEquals(List.of(ManagedObjectId.of(FDN)), subscriptionEntity.getGNodebMoIdList());
        assertTrue(subscriptionEntity.isActive());
    }

    @Test
    void mapEntityToSubscriptionDto() {
        var subscriptionEntity = getSubscriptionEntity(FDN);
        subscriptionEntity.setActive(true);
        var subscriptionDto = subscriptionEntity.toSubscriptionDto();
        assertEquals(subscriptionEntity.getEventIdList(), subscriptionDto.geteId());
        assertEquals(subscriptionEntity.getTrcLevelList(), subscriptionDto.getTrcLevels());
        assertEquals(List.of(NwFunction.CUUP), subscriptionDto.getNwFunctions());
        assertEquals(List.of(FDN), subscriptionDto.getGnodebNames());
        assertTrue(subscriptionDto.getActive());
    }

    public static Subscription getSubscriptionEntity(String... fdn) {
        Subscription subscriptionEntity = new Subscription();
        subscriptionEntity.setName(SUBSCRIPTION_NAME);
        subscriptionEntity.setStartTime(System.currentTimeMillis() + 2000);
        subscriptionEntity.setEventIdList(List.of(1L, 2L, 3L));
        subscriptionEntity.setTrcLevelList(List.of(TrcLevel.UE));
        subscriptionEntity.setEventMessageGroupList(List.of(PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_CUUP));
        subscriptionEntity.setGNodebMoIdList(Stream.of(fdn).map(ManagedObjectId::of).toList());
        return subscriptionEntity;
    }

    public static SubscriptionDto getSubscriptionDto(String... fdn) {
        var subscriptionDto = new SubscriptionDto();
        subscriptionDto.setStartTime(System.currentTimeMillis());
        subscriptionDto.seteId(List.of(1L, 2L, 3L));
        subscriptionDto.setTrcLevels(List.of(TrcLevel.UE));
        subscriptionDto.setNwFunctions(List.of(NwFunction.CUUP));
        subscriptionDto.setGnodebNames(List.of(fdn));
        return subscriptionDto;
    }

    public static SubscriptionDto getSubscriptionDto(long startTime, String... fdn) {
        var subscriptionDto = getSubscriptionDto(fdn);
        subscriptionDto.setStartTime(startTime);
        return subscriptionDto;
    }

}