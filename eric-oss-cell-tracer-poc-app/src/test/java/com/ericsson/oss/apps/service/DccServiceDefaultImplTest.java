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

import com.ericsson.oss.apps.config.DccConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Useless class for coverage
 */
class DccServiceDefaultImplTest {
    DccService dccService = (new DccConfig()).dccDefaultService();

    @Test
    void createIds() {
        dccService.createIds();
    }

    @Test
    void deleteIds() {
        dccService.deleteIds();
    }

    @Test
    void patchDccSubscription() {
        assertTrue(dccService.patchDccSubscription(null, null));
    }

    @Test
    void blankDccSubscription() {
        assertTrue(dccService.blankDccSubscription(null));
    }
}