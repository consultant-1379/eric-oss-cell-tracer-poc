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
package com.ericsson.oss.apps.controller.topology.nr;

import com.ericsson.oss.apps.ncmp.model.ManagedObjectId;
import com.ericsson.oss.apps.topology.TopologyService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@WebMvcTest(TopologyApiControllerImpl.class)
class TopologyApiControllerImplTest {

    private static final String TEST_FDN = "SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR01gNodeBRadio00001,ManagedElement=NR01gNodeBRadio00001,GNBDUFunction=1";

    @MockBean
    private TopologyService topologyService;
    @MockBean
    private WebClient webClient;

    @Autowired
    private MockMvc mvc;

    @Test
    void getAllNodeFdn() throws Exception {
        List<ManagedObjectId> nodes = List.of(ManagedObjectId.of(TEST_FDN));
        Mockito.when(topologyService.fetchAllNodeFdn()).thenReturn(nodes);
        mvc.perform(get("/v1/topology/nr/nodes")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("""
                        [{"name":"NR01gNodeBRadio00001","fdn":"SubNetwork=Europe,SubNetwork=Ireland,MeContext=NR01gNodeBRadio00001,ManagedElement=NR01gNodeBRadio00001,GNBDUFunction=1"}]"""));
        verify(topologyService, times(1)).fetchAllNodeFdn();
    }
}
