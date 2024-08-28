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
package com.ericsson.oss.apps.controller.cm.nr;

import com.ericsson.oss.apps.api.model.controller.GNodeb;
import com.ericsson.oss.apps.api.model.controller.NRCell;
import com.ericsson.oss.apps.service.CmService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
@WebMvcTest(CmApiControllerImpl.class)
class CmApiControllerImplTest {

    @MockBean
    private WebClient webClient;

    @MockBean
    CmService cmService;

    @Autowired
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getGnodebs() throws Exception {
        GNodeb gNodeb = new GNodeb(1234L, "a gnodeb");
        var gNodebList = List.of(gNodeb);
        org.mockito.Mockito.when(cmService.getGnodebs()).thenReturn(gNodebList);
        mvc.perform(get("/v1/cm/nr/gnodebs")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("""
                        [{"gnid":1234,"gnodebName":"a gnodeb"}]"""));
        verify(cmService, times(1)).getGnodebs();
    }

    @Test
    void getNrcells() throws Exception {
        NRCell nrCell = new NRCell(1234L, "a cell");
        var nrCellList = List.of(nrCell);
        org.mockito.Mockito.when(cmService.getNrCells(List.of(123L))).thenReturn(nrCellList);
        mvc.perform(get("/v1/cm/nr/nrcells")
                        .queryParam("gnid", "123")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("""
                        [{"nci":1234,"cellName":"a cell"}]"""));
        verify(cmService, times(1)).getNrCells(List.of(123L));
    }
}