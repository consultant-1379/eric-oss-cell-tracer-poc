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
package com.ericsson.oss.apps.topology;

import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.ncmp.util.CmHandleResolver;
import com.ericsson.oss.apps.topology.model.ExternalId;
import com.ericsson.oss.apps.topology.model.TopologyObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.ericsson.oss.apps.topology.IdentityUtils.buildFdn;
import static com.ericsson.oss.apps.topology.IdentityUtils.generateDnPrefixKeyCombinations;

@Slf4j
@RequiredArgsConstructor
public class TopologyService {

    private static final String NODE_TOPOLOGY_CACHE_KEY = "fetchAllNodeFdn";
    private static final List<String> NODE_RESOURCE_KEYS = List.of("ManagedElement", "GNBDUFunction");

    private final CtsRestClient ctsRestClient;
    private final CmHandleResolver cmHandleResolver;

    @Scheduled(cron = "${rapp-sdk.topology.cache.eviction-rate:0 0 0 * * *}")
    @CacheEvict(value = NODE_TOPOLOGY_CACHE_KEY, allEntries = true)
    public void evictAllCacheValues() {
        log.debug("Cache: {} evicted", NODE_TOPOLOGY_CACHE_KEY);
    }

    @Cacheable(value = NODE_TOPOLOGY_CACHE_KEY)
    public List<ManagedObjectId> fetchAllNodeFdn() {
        return ctsRestClient.fetchNodes()
                .map(this::extractNodeFdn)
                .toStream()
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<ManagedObjectId> extractNodeFdn(TopologyObject topologyObject) {
        List<String> values = Arrays.asList(topologyObject.getName().split("/"));
        ExternalId externalId = topologyObject.getExternalId();

        Optional<ManagedObjectId> nodeFdn = generateDnPrefixKeyCombinations(values.size() - NODE_RESOURCE_KEYS.size())
                .peek(keys -> keys.addAll(NODE_RESOURCE_KEYS))
                .map(keys -> buildFdn(keys, values))
                .map(ManagedObjectId::of)
                .filter(fdn -> verifyCmHandle(externalId, fdn))
                .findFirst();

        if (nodeFdn.isEmpty()) {
            log.warn("Couldn't find Fdn for {}", externalId);
        }
        return nodeFdn;
    }

    private boolean verifyCmHandle(ExternalId externalId, ManagedObjectId objectId) {
        return cmHandleResolver.getCmHandle(objectId)
                .map(e -> externalId.cmHandle().equals(e))
                .orElseGet(() -> {
                    log.warn("CmHandler resolver algorithm failed");
                    return false;
                });
    }
}
