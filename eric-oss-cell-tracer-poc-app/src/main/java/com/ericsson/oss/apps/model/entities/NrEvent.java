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

import com.ericsson.oss.apps.model.embeddables.EtcmVersionHeader;
import com.ericsson.oss.apps.model.embeddables.EventId;
import com.ericsson.oss.apps.model.embeddables.PmEventVersionHeader;
import com.ericsson.pm_event.PmEventOuterClass;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@MappedSuperclass
@FieldNameConstants
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class NrEvent {
    @EmbeddedId
    @EqualsAndHashCode.Include
    private EventId id;
    @Transient
    @EqualsAndHashCode.Include
    private static final Class<NrEvent> eventClass = NrEvent.class;
    @JdbcTypeCode(SqlTypes.JSON)
    private String asnContent;
    private Long timeStamp;
    private byte[] systemUuid;
    private Long eventId;
    private PmEventOuterClass.PmEventMessageGroup msgGroup;
    private String computeName;
    private String networkManagedElement;
    private String ueTraceId;
    private byte[] traceReference;
    private String traceRecordingSessionReference;
    @Embedded
    private PmEventVersionHeader pmEventVersionHeader;
    @Embedded
    private EtcmVersionHeader etcmVersionHeader;

    private byte[] mainPlmnId;
    private Long gnbId;
    private Long gnbIdLength;
    @JdbcTypeCode(SqlTypes.JSON)
    private String payload;
}
