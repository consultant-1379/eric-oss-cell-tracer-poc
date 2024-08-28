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
package com.ericsson.oss.apps.cleaner;

import com.ericsson.oss.apps.repository.NrCellPmEventRepository;
import com.ericsson.oss.apps.repository.NrNodePmEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class PmEventCleaner {

    private final NrNodePmEventRepository nrNodePmEventRepository;
    private final NrCellPmEventRepository nrCellPmEventRepository;
    private static final int ROP_MILLIS = 900000;
    @Value("${pm.ropTimeExpirePeriod}")
    int ropTimeExpirePeriod;

    @Scheduled(cron = "${pm.cleaningScheduler:0 */15 * * * *}")
    @Transactional
    void cleanPmEvents() {
        Long ropTime = getRopTime(System.currentTimeMillis(), ropTimeExpirePeriod);

        nrCellPmEventRepository.deleteByTimeStampLessThan(ropTime);
        nrNodePmEventRepository.deleteByTimeStampLessThan(ropTime);

    }

    Long getRopTime(long now, long timewarp) {
        return (now - now % ROP_MILLIS) - timewarp * ROP_MILLIS;
    }
}
