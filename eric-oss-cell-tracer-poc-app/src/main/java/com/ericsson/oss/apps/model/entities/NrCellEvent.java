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
package com.ericsson.oss.apps.model.entities;

import com.ericsson.oss.apps.model.mom.NRCellDU;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.SQLInsert;

@Entity
@Setter
@Getter
@ToString(callSuper = true)
@FieldNameConstants
@Table(indexes = {
        @Index(name = "nrcellevent_timestamp_index", columnList = "time_stamp"),
        @Index(name = "nrcellevent_gnbid_index", columnList = "gnb_id"),
        @Index(name = "nrcellevent_nci_index", columnList = "nci"),
        @Index(name = "nrcellevent_msg_group_index", columnList = "msg_group"),
        @Index(name = "nrcellevent_event_id_index", columnList = "event_id"),
})
// Alphabetic Order - (fields, keys)
@SQLInsert( sql = """
INSERT INTO nr_cell_event (
  asn_content,
  compute_name,
  etcm_version,
  etcm_correction_version,
  event_id,
  gnb_id,
  gnb_id_length,
  main_plmn_id,
  msg_group,
  nci,
  network_managed_element,
  payload,
  pm_event_group_version,
  pm_event_common_version,
  pm_event_correction_version,
  system_uuid,
  time_stamp,
  trace_recording_session_reference,
  trace_reference,
  ue_trace_id,
  msg_offset,
  msg_partition
)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
ON CONFLICT DO NOTHING
""")
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class NrCellEvent extends NrEvent {
    private Long nCI;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinFormula(value = "NCI", referencedColumnName = "NCI")
    private NRCellDU nrCellDU;
}
