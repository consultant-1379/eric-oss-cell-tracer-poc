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
package com.ericsson.oss.apps.config;

import com.ericsson.oss.apps.bdr.client.BdrClient;
import com.ericsson.oss.apps.bdr.loader.FileTracker;
import com.ericsson.oss.apps.loader.AllowedNodeLoader;
import com.ericsson.oss.apps.loader.AllowedNodeMap;
import com.ericsson.oss.apps.loader.NREventClassLoader;
import com.ericsson.oss.apps.loader.NREventClassMap;
import com.ericsson.oss.apps.model.schema.EventClassSchema;
import com.ericsson.oss.apps.model.schema.NodeIdentitySchema;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;

@Slf4j
@ConditionalOnExpression("!'${app.data.config:}'.isEmpty()")
@Configuration
@RequiredArgsConstructor
public class LoaderConfiguration {

    private final NREventClassMap eventClassMap;
    private final AllowedNodeMap allowedNodeMap;
    @Value("${app.data.config:}")
    private String dataConfig;

    @PostConstruct
    public void load() {
        BdrClient bdrClient = classPathBdrClient();
        FileTracker fileTracker = new FileTracker();
        new NREventClassLoader(EventClassSchema.class, bdrClient, fileTracker, eventClassMap::putEventClass).loadData(dataConfig);
        new AllowedNodeLoader(NodeIdentitySchema.class, bdrClient, fileTracker, allowedNodeMap::addNode).loadData(dataConfig);
    }

    private BdrClient classPathBdrClient() {
        return new BdrClient() {
            @Override
            public boolean exists(String path) {
                return new ClassPathResource(path).exists();
            }

            @SneakyThrows
            @Override
            public InputStream getObject(String objectPath) {
                return new FileInputStream(ResourceUtils.getFile(objectPath));
            }

            @Override
            public boolean uploadObject(String objectPath, InputStream inputStream) {
                return false; // not allowed for classPath
            }

            @Override
            public Optional<String> getCheckSum(String objectPath) {
                return Optional.of(objectPath);
            }
        };
    }
}
