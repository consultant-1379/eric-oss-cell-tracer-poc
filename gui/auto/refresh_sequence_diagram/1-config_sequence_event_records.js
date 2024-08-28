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
let url = "http://eric-oss-cell-tracer-poc:8080/v1/event/nr/events";
let filters = {nci: [], eId: [], gnid: [], nwFunction: [], trcLevel: []};

url = url + "?start=" + args.link.trace_date_select.data.start + "&end=" + args.link.trace_date_select.data.end;

if (args.link.trace_date_select.data.ueTraceId.length > 0) {
    url += "&ueTraceId=" + args.link.trace_date_select.data.ueTraceId;
}
if (args.link.trace_date_select.data.traceRecordingSessionReference.length > 0) {
    url += "&traceRecordingSessionReference=" + args.link.trace_date_select.data.traceRecordingSessionReference;
}

args.link.trace_tree_select.selected.forEach((element) => {
    if (element.data == "gnid") {
        filters.gnid = [];
        filters.nci = [];
        element.children.forEach((node) => {
            filters.gnid.push(node.data);
            filters.nci = filters.nci.concat(node.children.map(cell => cell.data));
        });
    } else {
        filters[element.data] = element.children.map(item => item.data);
    }
})

if (filters.nci.length > 0) {
    url += "&nci=" + filters.nci;
}
if (filters.eId.length > 0) {
    url += "&eId=" + filters.eId;
}
if (filters.gnid.length > 0) {
    url += "&gnid=" + filters.gnid;
}
if (filters.nwFunction.length > 0) {
    url += "&nwFunction=" + filters.nwFunction;
}
if (filters.trcLevel.length > 0) {
    url += "&trcLevel=" + filters.trcLevel;
}


result = {
    "url": url,
    "options": {
        "method": "GET",
        "timeout": 30000,
        "headers": {
            "content-type": "application/json"
        }
    },
    "responseFormat": "json"
}