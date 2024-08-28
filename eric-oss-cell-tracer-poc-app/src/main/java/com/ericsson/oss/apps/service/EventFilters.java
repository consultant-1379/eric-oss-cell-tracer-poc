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

import com.ericsson.pm_event.PmEventOuterClass;

import java.util.List;
import java.util.Optional;

public record EventFilters(String ueTraceId,
                           String traceRecordingSessionReference,
                           long start, long end, List<Long> gnid, List<Long> nci,
                           Optional<List<Long>> eId, List<PmEventOuterClass.PmEventMessageGroup> pmEventMessageGroupList) {
}
