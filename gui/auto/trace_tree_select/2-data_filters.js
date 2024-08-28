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

function addChildren(parent, children) {
    if (children.length > 0) {
        parent.children = children;
    }
    return parent;
}

const nodes = results.cm
  .map((node) => addChildren(
      {"label": node.gnodebName, "data": node.gnid},
      node.cells.map((cell) => ({
          "label": cell.cellName,
          "data": cell.nci
      })).sort((a,b) => sortByLabel(a,b))
  )).sort((a,b) => sortByLabel(a,b));

const trcLevels = results.filters.trcLevels
  .map((level) => ({
    "label": level,
    "data": level
  })).sort((a,b) => sortByLabel(a,b));

const nwFunctions = results.filters.nwFunctions
  .map((nwFunction) => ({
    "label": nwFunction,
    "data": nwFunction
  })).sort((a,b) => sortByLabel(a,b));

const events = results.filters.eventTypes
  .map((event) => ({
    "label": event.eventTypeName,
    "data": event.eId
  })).sort((a,b) => sortByLabel(a,b));

result = [
    addChildren({"label": "Node", "data": "gnid"}, nodes),
    addChildren({"label": "Trace Level", "data": "trcLevel"}, trcLevels),
    addChildren({"label": "Node Function type", "data": "nwFunction"}, nwFunctions),
    addChildren({"label": "PM Event", "data": "eId"}, events)
];
