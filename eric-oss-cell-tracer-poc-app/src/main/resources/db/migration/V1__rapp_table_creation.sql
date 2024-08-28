--
-- COPYRIGHT Ericsson 2023
--
--
--
-- The copyright to the computer program(s) herein is the property of
--
-- Ericsson Inc. The programs may be used and/or copied only with written
--
-- permission from Ericsson Inc. or in accordance with the terms and
--
-- conditions stipulated in the agreement/contract under which the
--
-- program(s) have been supplied.
--

CREATE TABLE gnbdufunction
(
    gnbid          BIGINT,
    gnbid_length   INTEGER,
    parent_res_ref VARCHAR(255),
    mcc            INTEGER,
    mnc            INTEGER,
    me_fdn         VARCHAR(255) NOT NULL,
    res_ref        VARCHAR(255) NOT NULL,
    CONSTRAINT pk_gnbdufunction PRIMARY KEY (me_fdn, res_ref)
);

CREATE TABLE nr_cell_event
(
    asn_content                       JSONB,
    time_stamp                        BIGINT,
    system_uuid                       BYTEA,
    event_id                          BIGINT,
    msg_group                         INTEGER,
    compute_name                      VARCHAR(255),
    network_managed_element           VARCHAR(255),
    ue_trace_id                       VARCHAR(255),
    trace_reference                   BYTEA,
    trace_recording_session_reference VARCHAR(255),
    main_plmn_id                      BYTEA,
    gnb_id                            BIGINT,
    gnb_id_length                     BIGINT,
    payload                           JSONB,
    nci                               BIGINT,
    msg_offset                        BIGINT  NOT NULL,
    msg_partition                     INTEGER NOT NULL,
    etcm_version                      BIGINT,
    etcm_correction_version           BIGINT,
    nr_celldu_me_fdn                  VARCHAR(255),
    nr_celldu_res_ref                 VARCHAR(255),
    pm_event_group_version            BIGINT,
    pm_event_common_version           BIGINT,
    pm_event_correction_version       BIGINT,
    CONSTRAINT pk_nrcellevent PRIMARY KEY (msg_offset, msg_partition)
);

CREATE TABLE nr_node_event
(
    asn_content                       JSONB,
    time_stamp                        BIGINT,
    system_uuid                       BYTEA,
    event_id                          BIGINT,
    msg_group                         INTEGER,
    compute_name                      VARCHAR(255),
    network_managed_element           VARCHAR(255),
    ue_trace_id                       VARCHAR(255),
    trace_reference                   BYTEA,
    trace_recording_session_reference VARCHAR(255),
    main_plmn_id                      BYTEA,
    gnb_id                            BIGINT,
    gnb_id_length                     BIGINT,
    payload                           JSONB,
    msg_offset                        BIGINT  NOT NULL,
    msg_partition                     INTEGER NOT NULL,
    etcm_version                      BIGINT,
    etcm_correction_version           BIGINT,
    g_nodeb_me_fdn                    VARCHAR(255),
    g_nodeb_res_ref                   VARCHAR(255),
    pm_event_group_version            BIGINT,
    pm_event_common_version           BIGINT,
    pm_event_correction_version       BIGINT,
    CONSTRAINT pk_nrnodeevent PRIMARY KEY (msg_offset, msg_partition)
);

CREATE TABLE nrcelldu
(
    cell_local_id        INTEGER,
    parent_res_ref       VARCHAR(255),
    administrative_state SMALLINT,
    nci                  BIGINT,
    me_fdn               VARCHAR(255) NOT NULL,
    res_ref              VARCHAR(255) NOT NULL,
    g_nodeb_me_fdn       VARCHAR(255),
    g_nodeb_res_ref      VARCHAR(255),
    CONSTRAINT pk_nrcelldu PRIMARY KEY (me_fdn, res_ref)
);
CREATE TABLE nrsector_carrier
(
    me_fdn                   VARCHAR(255),
    res_ref                  VARCHAR(255),
    parent_res_ref           VARCHAR(255),
    nrsector_carrier_id      VARCHAR(255),
    operational_state        SMALLINT,
    CONSTRAINT pk_nrsector_carrier PRIMARY KEY (me_fdn, res_ref)
);

CREATE TABLE subscription
(
    name       VARCHAR(255) NOT NULL,
    end_time   BIGINT,
    start_time BIGINT       NOT NULL,
    active     BOOLEAN      NOT NULL,
    CONSTRAINT pk_subscription PRIMARY KEY (name)
);

CREATE TABLE subscription_event_id_list
(
    subscription_name VARCHAR(255) NOT NULL,
    event_id_list     BIGINT
);

CREATE TABLE subscription_event_message_group_list
(
    subscription_name        VARCHAR(255) NOT NULL,
    event_message_group_list INTEGER
);

CREATE TABLE subscription_g_nodeb_mo_id_list
(
    subscription_name VARCHAR(255) NOT NULL,
    me_fdn            VARCHAR(255),
    res_ref           VARCHAR(255)
);

CREATE TABLE subscription_trc_level_list
(
    subscription_name VARCHAR(255) NOT NULL,
    trc_level_list    SMALLINT
);

CREATE INDEX idx_gnodeb_gnbid ON gnbdufunction (gnbid);

CREATE INDEX nrcellevent_event_id_index ON nr_cell_event (event_id);

CREATE INDEX nrcellevent_gnbid_index ON nr_cell_event (gnb_id);

CREATE INDEX nrcellevent_msg_group_index ON nr_cell_event (msg_group);

CREATE INDEX nrcellevent_nci_index ON nr_cell_event (nci);

CREATE INDEX nrcellevent_timestamp_index ON nr_cell_event (time_stamp);

CREATE INDEX nrnodeevent_event_id_index ON nr_node_event (event_id);

CREATE INDEX nrnodeevent_gnbid_index ON nr_node_event (gnb_id);

CREATE INDEX nrnodeevent_msg_group_index ON nr_node_event (msg_group);

CREATE INDEX nrnodeevent_timestamp_index ON nr_node_event (time_stamp);

ALTER TABLE nrcelldu
    ADD CONSTRAINT FK_NRCELLDU_ON_GNMEFDGNRERE FOREIGN KEY (g_nodeb_me_fdn, g_nodeb_res_ref) REFERENCES gnbdufunction (me_fdn, res_ref);

ALTER TABLE nr_cell_event
    ADD CONSTRAINT FK_NRCELLEVENT_ON_NRMEFDNRRERE FOREIGN KEY (nr_celldu_me_fdn, nr_celldu_res_ref) REFERENCES nrcelldu (me_fdn, res_ref);

ALTER TABLE nr_node_event
    ADD CONSTRAINT FK_NRNODEEVENT_ON_GNMEFDGNRERE FOREIGN KEY (g_nodeb_me_fdn, g_nodeb_res_ref) REFERENCES gnbdufunction (me_fdn, res_ref);

ALTER TABLE subscription_event_id_list
    ADD CONSTRAINT fk_subscription_eventidlist_on_subscription FOREIGN KEY (subscription_name) REFERENCES subscription (name);

ALTER TABLE subscription_event_message_group_list
    ADD CONSTRAINT fk_subscription_eventmessagegrouplist_on_subscription FOREIGN KEY (subscription_name) REFERENCES subscription (name);

ALTER TABLE subscription_g_nodeb_mo_id_list
    ADD CONSTRAINT fk_subscription_gnodebmoidlist_on_subscription FOREIGN KEY (subscription_name) REFERENCES subscription (name);

ALTER TABLE subscription_trc_level_list
    ADD CONSTRAINT fk_subscription_trclevellist_on_subscription FOREIGN KEY (subscription_name) REFERENCES subscription (name);