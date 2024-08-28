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

package com.ericsson.oss.apps.db;

import com.ericsson.oss.apps.CoreApplicationTest;
import com.ericsson.oss.apps.model.embeddables.EtcmVersionHeader;
import com.ericsson.oss.apps.model.embeddables.EventId;
import com.ericsson.oss.apps.model.embeddables.PmEventVersionHeader;
import com.ericsson.oss.apps.model.entities.NrCellEvent;
import com.ericsson.oss.apps.model.entities.NrEvent;
import com.ericsson.oss.apps.model.entities.NrNodeEvent;
import com.ericsson.oss.apps.model.mom.NRCellDU;
import com.ericsson.oss.apps.repository.CmNrCellDuRepo;
import com.ericsson.oss.apps.service.PmEventService;
import com.ericsson.pm_event.PmEventOuterClass;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainerProvider;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@ActiveProfiles("postgres")
@SpringBootTest(classes = {CoreApplicationTest.class}, properties = {"logging.level.org.flywaydb=DEBUG"})
class PostgreSQLIntegrationTest {

    @Container
    private static final JdbcDatabaseContainer postgreSQL = new PostgreSQLContainerProvider()
            .newInstance();

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    public PmEventService eventService;
    @Autowired
    public ObjectMapper objectMapper;
    @Autowired
    public EntityManager em;
    @Autowired
    CmNrCellDuRepo cmNrCellDuRepo;

    @DynamicPropertySource
    static void postgresqlProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQL::getJdbcUrl);
        registry.add("spring.datasource.password", postgreSQL::getPassword);
        registry.add("spring.datasource.username", postgreSQL::getUsername);
    }

    @Test
    @Order(1)
    void testDatabaseConnection() {
        // Execute a query to test DB connection
        int result = jdbcTemplate.queryForObject("SELECT 42", Integer.class);
        assertEquals(42, result);
    }

    @Test
    @Order(2)
    void testQuery() {
        // Insert a nrcelldu into the database
        NRCellDU cellDU = new NRCellDU("fdn1");
        cellDU.setNCI(456L);
        cellDU.setCellLocalId(123);
        cmNrCellDuRepo.save(cellDU);
        String query = "SELECT cell_local_id, nci FROM nrcelldu WHERE ME_FDN LIKE 'fdn1'; ";

        jdbcTemplate.query(query, rs -> {
            int cell_local_id = rs.getInt(1);
            int nci = rs.getInt(2);

            assertEquals(123, cell_local_id);
            assertEquals(456, nci);
        });
    }

    @Test
    @Order(3)
    void testEventPersisting() throws JsonProcessingException {
        String nodePayload = objectMapper.writeValueAsString(new TestObject(1));
        NrEvent nodeEvent = createEvent(EventType.NR_NODE_EVENT, 2, 3L, nodePayload);
        String cellPayload = objectMapper.writeValueAsString(new TestObject(2));
        NrEvent cellEvent = createEvent(EventType.NR_CELL_EVENT, 2, 4L, cellPayload);
        cellEvent.setMsgGroup(PmEventOuterClass.PmEventMessageGroup.UNRECOGNIZED);
        eventService.batchInsertEvents(List.of(nodeEvent, nodeEvent, cellEvent, cellEvent));

        NRCellDU nrCellDU = new NRCellDU("SubNetwork=Unknown,MeContext=M9AT2772B2,ManagedElement=M9AT2772B2,GNBDUFunction=1,NRCellDU=K9AT2772B11");
        nrCellDU.setNCI(23423423L);
        cmNrCellDuRepo.save(nrCellDU);

        var retrievedCellEvent = eventService.findAllCellEvent().get(0);
        NRCellDU cellDuFromEvent = retrievedCellEvent.getNrCellDU();
        Assertions.assertNotNull(cellDuFromEvent);
        assertEquals(PmEventOuterClass.PmEventMessageGroup.UNRECOGNIZED, retrievedCellEvent.getMsgGroup());
        Assertions.assertEquals(1, eventService.findAllNodeEvent().size());
        Assertions.assertEquals(1, eventService.findAllCellEvent().size());

        int nodesCount = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM nr_node_event e WHERE e.payload->'foo' = '1'").getSingleResult()).intValue();
        Assertions.assertEquals(1, nodesCount);
        int cellsCount = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM nr_cell_event e WHERE e.payload->'foo' = '1'").getSingleResult()).intValue();
        Assertions.assertEquals(0, cellsCount);
        var asnContentCount = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM nr_cell_event e WHERE e.asn_content->>'decoded' = 'ASN1'").getSingleResult()).intValue();
        Assertions.assertEquals(1, asnContentCount);
    }

    record TestObject(int foo) {}

    public enum EventType {
        NR_NODE_EVENT,
        NR_CELL_EVENT;
    }

    public NrEvent createEvent(EventType eventType, int msgPartition, long msgOffset, String payload) {
        NrEvent event;
        if (EventType.NR_NODE_EVENT.equals(eventType)) {
            event = new NrNodeEvent();
        } else {
            NrCellEvent cellEvent = new NrCellEvent();
            cellEvent.setNCI(23423423L);
            event = cellEvent;
        }
        event.setId(new EventId(msgOffset, msgPartition));
        event.setMsgGroup(PmEventOuterClass.PmEventMessageGroup.PM_EVENT_MESSAGE_GROUP_COMMON);
        event.setEventId(1L);
        event.setComputeName("NodeName");
        event.setNetworkManagedElement("NE");
        event.setTraceReference(new byte[]{0x00, 0x00, 0x00, 0x00, 0x01, (byte) 0x80, 0x00, 0x03});
        event.setTraceRecordingSessionReference(Base64.getEncoder().encodeToString(new byte[]{0x00, 0x00, 0x00, 0x00, 0x01, (byte) 0x80, 0x00, 0x03}));
        event.setUeTraceId(Base64.getEncoder().encodeToString(new byte[]{0x00, 0x00, 0x00, 0x00, 0x01, (byte) 0x80, 0x00, 0x03}));
        event.setEtcmVersionHeader(new EtcmVersionHeader(2L,3L));
        event.setPmEventVersionHeader(new PmEventVersionHeader(2L, 2L, 4L));
        event.setPayload(payload);
        event.setAsnContent("{\"decoded\" : \"ASN1\"}");
        return event;
    }
}
