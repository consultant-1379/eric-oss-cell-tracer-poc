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

url = url + "?start=" + (Date.now() - 600000) + "&end=" + Date.now();


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