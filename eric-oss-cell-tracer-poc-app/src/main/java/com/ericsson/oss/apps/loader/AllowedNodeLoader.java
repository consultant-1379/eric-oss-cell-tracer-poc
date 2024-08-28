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

import com.ericsson.oss.apps.bdr.client.BdrClient;
import com.ericsson.oss.apps.bdr.loader.CsvLoader;
import com.ericsson.oss.apps.bdr.loader.FileTracker;
import com.ericsson.oss.apps.model.AllowedNode;
import com.ericsson.oss.apps.model.schema.NodeIdentitySchema;
import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
public class AllowedNodeLoader extends CsvLoader<NodeIdentitySchema, AllowedNode> {

    public static final String PATH_TEMPLATE = "%sallowed_nodes.csv";

    public AllowedNodeLoader(
            Class<NodeIdentitySchema> clazz,
            BdrClient bdrClient,
            FileTracker fileTracker,
            Consumer<AllowedNode> recordConsumer
    ) {
        super(clazz, PATH_TEMPLATE, bdrClient, fileTracker, recordConsumer);
    }

    @Override
    protected InputStream interceptObjectInputStream(InputStream inputStream) {
        return inputStream;
    }

    @Override
    protected Optional<AllowedNode> transformData(NodeIdentitySchema nodeIdentitySchema) {
        if (nodeIdentitySchema.getFdn() == null) {
            log.warn("no fdn in {}", nodeIdentitySchema);
            return Optional.empty();
        }
        return Optional.of(new AllowedNode(ManagedObjectId.of(nodeIdentitySchema.getFdn()), Optional.ofNullable(nodeIdentitySchema.getGnbId())));
    }

}
