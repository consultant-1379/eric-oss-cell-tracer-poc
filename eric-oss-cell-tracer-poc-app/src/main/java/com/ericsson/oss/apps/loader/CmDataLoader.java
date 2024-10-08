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

import com.ericsson.oss.apps.model.mom.GNBDUFunction;
import com.ericsson.oss.apps.ncmp.NcmpClient;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequiredArgsConstructor
public class CmDataLoader {
    private final NcmpClient ncmpClient;

    @Value("${cm-extract.batchSize}")
    private int batchSize;

    private final Lock taskLock = new ReentrantLock();


    @Async
    public void extractCmInfo(List<ManagedObjectId> allowedNodes) {
        if (taskLock.tryLock()) {
            try {
                log.debug("refreshing CM info");
                if (allowedNodes.isEmpty()) {
                    log.warn("CmLoader: allowedNodes list is empty");
                    return;
                }
                IntStream.range(0, allowedNodes.size())
                        .boxed()
                        .collect(Collectors.groupingBy(index -> index / batchSize,
                                Collectors.mapping(allowedNodes::get, Collectors.toList())))
                        .forEach((integer, managedObjectIdList) -> extractCMData(managedObjectIdList));
            } finally {
                taskLock.unlock();
                log.debug("finished refreshing CM info");
            }
        } else {
            log.warn("CM info refresh in progress, task aborted.");
        }

    }

    @Transactional
    private void extractCMData(List<ManagedObjectId> managedObjectIdList) {
        managedObjectIdList.parallelStream().forEach(managedObjectId -> {
            try {
                Optional<GNBDUFunction> mo = ncmpClient.getCmResource(managedObjectId, GNBDUFunction.class);
                if (mo.isEmpty()) {
                    log.warn("CmLoader: No CM found for {}", managedObjectId);
                }
            } catch (RuntimeException e) {
                log.warn("CmLoader: Couldn't retrieved CM for {}", managedObjectId, e);
            }

        });
    }

}
