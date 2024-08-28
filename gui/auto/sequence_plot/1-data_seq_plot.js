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
const customOrder = { "UE": 1, "RC": 2, "RP": 3, "PP": 4 , "ENODEB":5, "GNODEB":6};

function getColor(data) {
    return data.toUpperCase().includes("FAIL") ? "var(--red)" : "var(--blue)";
}

// Generating block list part and removing duplicates
const blockUniq = [...new Set([...results.sequence.map(row => row.networkElement1), ...results.sequence.map(row => row.networkElement2)])];

// Sorting blockUniq based on the custom order and mapping to block_result
const block_result = blockUniq
    .sort((a, b) => (customOrder[a] || Infinity) - (customOrder[b] || Infinity))
    .map(row => ({ title: row }));


const regex = /ue_trace_id:\s*([a-zA-Z0-9=?/]+)/;

function extractUETraceId(text, raw) {
    const match = text.match(regex, raw);

    if (match && match.length > 1) {
        return match[1];
    } else {
        return raw;
    }
}

// Generating row list part
const rows = results.sequence.map(row => ({
    date: row.timestamp,
    message: `${row.eventType.eventTypeName} - ${extractUETraceId(row.asn1Content, row.ueTraceId)} / ${JSON.parse(row.body).nci}`,
    start: blockUniq.indexOf(row.networkElement1),
    end: blockUniq.indexOf(row.networkElement2),
    color: getColor(row.eventType.eventTypeName),
    shape: row.messageDirection === "OUTGOING" ? "arrow" : "line",
    details: {
        ...JSON.parse(row.body),
        ...JSON.parse(row.asn1Content),
        messageDirection: row.messageDirection,
        networkElement_1: row.networkElement1,
        networkElement_2: row.networkElement2
    }
}));

// Sorting rows array based on the date property in ascending order
rows.sort((a, b) => new Date(a.date) - new Date(b.date));

// Final merging part for the result
result = { blocks: block_result, rows: rows };