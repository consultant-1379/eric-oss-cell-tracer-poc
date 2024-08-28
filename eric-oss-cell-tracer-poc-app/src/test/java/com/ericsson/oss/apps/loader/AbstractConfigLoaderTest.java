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
import com.ericsson.oss.apps.bdr.loader.FileTracker;
import org.mockito.Mock;

abstract class AbstractConfigLoaderTest {
    static final String RESOURCE_FOLDER = "classpath:";
    static final String E_TAG = "d41d8cd98f00b204e9800998ecf8427e-2";
    final FileTracker fileTracker = new FileTracker();
    @Mock
    BdrClient bdrClient;

}
