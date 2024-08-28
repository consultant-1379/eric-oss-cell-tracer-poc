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

import com.ericsson.oss.apps.model.mom.GNBDUFunction;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.SQLInsert;

@Entity
@Getter
@Setter
@Table(indexes = {
        @Index(name = "nrnodeevent_timestamp_index", columnList = "time_stamp"),
        @Index(name = "nrnodeevent_gnbid_index", columnList = "gnb_id"),
        @Index(name = "nrnodeevent_msg_group_index", columnList = "msg_group"),
        @Index(name = "nrnodeevent_event_id_index", columnList = "event_id"),
})
// Alphabetic Order - (fields, keys)
@SQLInsert(sql = """
        INSERT INTO nr_node_event (
          asn_content,
          compute_name,
          etcm_version,
          etcm_correction_version,
          event_id,
          gnb_id,
          gnb_id_length,
          main_plmn_id,
          msg_group,
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
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        ON CONFLICT DO NOTHING
        """)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class NrNodeEvent extends NrEvent {
    @ManyToOne
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "GNB_ID", referencedColumnName = "GNBID")),
            @JoinColumnOrFormula(formula = @JoinFormula(value = "GNB_ID_LENGTH", referencedColumnName = "GNBIDLENGTH"))
    })
    private GNBDUFunction gNodeB;
}
