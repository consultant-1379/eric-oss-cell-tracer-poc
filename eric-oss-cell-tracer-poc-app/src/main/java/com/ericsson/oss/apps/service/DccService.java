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

import java.util.List;
import java.util.Map;

public interface DccService {
    default void createIds() {
    }

    default void deleteIds() {
    }

    default boolean patchDccSubscription(String name, Map<String, List<String>> predicates) {
        return true;
    }

    default boolean blankDccSubscription(String name) {
        return true;
    }

}
