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
  
function sortByLabel(a, b) {
      if (a.label < b.label) {
        return -1;
      }
      if (a.label > b.label) {
          return 1;
      } 
      return 0;
}

if (results.subscription.GNodebNames === undefined) {
    results.subscription.GNodebNames = [];
}
    if (results.subscription.trcLevels === undefined) {
    results.subscription.trcLevels = [];
}
    if (results.subscription.nwFunctions === undefined) {
    results.subscription.nwFunctions = [];
}
    if (results.subscription.eId === undefined) {
    results.subscription.eId = [];
}

const nodes = results.topology
  .map((node) => ({
      "label": node.name,
      "data": node.fdn,
      "selected": results.subscription.GNodebNames.includes(node.fdn) ? true : false
    })).sort((a,b) => sortByLabel(a,b));

const trcLevels = results.filters.trcLevels
  .map((level) => ({
    "label": level,
    "data": level,
    "selected": results.subscription.trcLevels.includes(level) ? true : false
  })).sort((a,b) => sortByLabel(a,b));

const nwFunctions = results.filters.nwFunctions
  .map((nwFunction) => ({
    "label": nwFunction,
    "data": nwFunction,
    "selected": results.subscription.nwFunctions.includes(nwFunction) ? true : false
  })).sort((a,b) => sortByLabel(a,b));

const events = results.filters.eventTypes
  .map((event) => ({
    "label": event.eventTypeName,
    "data": event.eId,
    "selected": results.subscription.eId.includes(event.eId) ? true : false
  })).sort((a,b) => sortByLabel(a,b));

result = [
    {
        "label": "Node",
        "data": "GNodebNames",
        "children": nodes
    },
    {
        "label": "Trace Level",
        "data": "trcLevels",
        "children": trcLevels
    },
    {
        "label": "Node Function type",
        "data": "nwFunctions",
        "children": nwFunctions
    },
    {
        "label": "PM Event",
        "data": "eId",
        "children": events
    }
];

