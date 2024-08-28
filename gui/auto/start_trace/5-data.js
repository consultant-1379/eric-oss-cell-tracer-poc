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

result.status = "Active"
result.startDateStatus = "Not specified"
result.endDateStatus = "Not specified"
 
let now = new Date().getTime();
if (args.link.trace_form.data.schedule) {
    result.startDateStatus = new Date(args.link.trace_form.data.startTime);
    if (now < result.startDateStatus) {
        result.status = "Inactive"
    }
    
    if (args.link.trace_form.data.until) {
        result.endDateStatus = new Date(args.link.trace_form.data.endTime);
    }
}
