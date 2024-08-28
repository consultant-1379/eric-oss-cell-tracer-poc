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

import com.ericsson.oss.apps.model.AllowedNode;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AllowedNodeMap {

    private final Map<ManagedObjectId, Optional<Long>> allowedNodeSet = new ConcurrentHashMap<>();

    public boolean isAllowed(ManagedObjectId managedObjectId) {
        return allowedNodeSet.containsKey(managedObjectId);
    }

    public void addNode(AllowedNode allowedNode) {
        allowedNodeSet.put(allowedNode.managedObjectId(), allowedNode.gnbId());
    }

    public List<ManagedObjectId> getAllNodes() {
        return allowedNodeSet.keySet().stream().toList();
    }

    public List<AllowedNode> getAllAllowedNodes() {
        return allowedNodeSet.entrySet().stream()
                .map(e -> new AllowedNode(e.getKey(), e.getValue()))
                .toList();
    }
}
