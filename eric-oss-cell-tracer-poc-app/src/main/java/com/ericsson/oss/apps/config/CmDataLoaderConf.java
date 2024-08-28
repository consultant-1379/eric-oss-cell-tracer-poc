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

import com.ericsson.oss.apps.loader.CmDataLoader;
import com.ericsson.oss.apps.model.mom.GNBDUFunction;
import com.ericsson.oss.apps.model.mom.NRCellDU;
import com.ericsson.oss.apps.ncmp.NcmpClient;
import com.ericsson.oss.apps.ncmp.model.ManagedObject;
import com.ericsson.oss.apps.ncmp.util.ManagedObjectAggregate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@ConditionalOnProperty(prefix = "rapp-sdk.ncmp", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CmDataLoaderConf {

    @Bean
    public CmDataLoader cmDataLoader(NcmpClient ncmpClient) {
        return new CmDataLoader(ncmpClient);
    }

    @Bean
    public ManagedObjectAggregate gNobeBAggregate() {
        return new ManagedObjectAggregate() {
            @Override
            public Class<? extends ManagedObject> getKey() {
                return GNBDUFunction.class;
            }

            @Override
            public Set<Class<? extends ManagedObject>> getTypes() {
                return Set.of(GNBDUFunction.class, NRCellDU.class);
            }
        };
    }
}
