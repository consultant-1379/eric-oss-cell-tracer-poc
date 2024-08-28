/*
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
 */

const regex = /ue_trace_id:\s*([a-zA-Z0-9=?/]+)/;

function extractUETraceId(text, raw) {
    const match = text.match(regex, raw);

    if (match && match.length > 1) {
        return match[1];
    } else {
        return raw;
    }
}

result = results.event_records.map((row) => ({
  "timestamp": new Date(row.timestamp).toISOString(),
  "gnodebName": row.gnodebName,
  "eventTypeName": row.eventType.eventTypeName,
  "nwFunction": row.eventType.nwFunction,
  "trcLevel": row.eventType.trcLevel,
  "ueTraceId": extractUETraceId(row.asn1Content, row.ueTraceId),
  "traceRecordingSessionReference": row.traceRecordingSessionReference,
  "body": {
        ...JSON.parse(row.body),
        ...JSON.parse(row.asn1Content)
    }
}));
