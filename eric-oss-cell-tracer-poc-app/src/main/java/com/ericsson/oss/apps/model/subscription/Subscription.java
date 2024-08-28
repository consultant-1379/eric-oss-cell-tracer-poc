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


import com.ericsson.oss.apps.api.model.controller.SubscriptionDto;
import com.ericsson.oss.apps.api.model.controller.TrcLevel;
import com.ericsson.oss.apps.model.converters.PmEventMessageGroupMapper;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.pm_event.PmEventOuterClass;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.ericsson.oss.apps.service.SubscriptionService.SUBSCRIPTION_NAME;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Subscription implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    String name;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<ManagedObjectId> gNodebMoIdList = new ArrayList<>();
    @ElementCollection(fetch = FetchType.EAGER)
    private List<PmEventOuterClass.PmEventMessageGroup> eventMessageGroupList = new ArrayList<>();
    @ElementCollection(fetch = FetchType.EAGER)
    private List<TrcLevel> trcLevelList = new ArrayList<>();
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Long> eventIdList = new ArrayList<>();
    Long endTime;
    long startTime;
    boolean active = false;

    public Subscription(SubscriptionDto subscriptionDto) {
        this.setActive(subscriptionDto.getActive());
        this.setName(SUBSCRIPTION_NAME);
        this.setEventIdList(subscriptionDto.geteId());
        this.setGNodebMoIdList(subscriptionDto.getGnodebNames().stream()
                .map(ManagedObjectId::of).collect(Collectors.toList()));
        this.setEventMessageGroupList(subscriptionDto.getNwFunctions().stream()
                .map(PmEventMessageGroupMapper::nwFunctionDtoToEntity).collect(Collectors.toList()));
        this.setTrcLevelList(subscriptionDto.getTrcLevels());
        this.setStartTime(subscriptionDto.getStartTime());
        this.setEndTime(subscriptionDto.getEndTime());
    }

    public SubscriptionDto toSubscriptionDto() {
        SubscriptionDto subscriptionDto = new SubscriptionDto();
        subscriptionDto.setActive(isActive());
        subscriptionDto.seteId(getEventIdList());
        subscriptionDto.setGnodebNames(getGNodebMoIdList().stream().map(ManagedObjectId::toString).toList());
        subscriptionDto.setTrcLevels(getTrcLevelList());
        subscriptionDto.setNwFunctions(getEventMessageGroupList().stream().map(PmEventMessageGroupMapper::nwFunctionEntityToDto).toList());
        subscriptionDto.setStartTime(getStartTime());
        subscriptionDto.setEndTime(getEndTime());
        return subscriptionDto;
    }

}
