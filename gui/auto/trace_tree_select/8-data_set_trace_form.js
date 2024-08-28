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

result = {
    end: new Date().getTime() + 7200000,
    status: results.subscription.active ? "Active" : "Inactive"
};
 
 
if (results.subscription.startTime !== undefined) {
    result.startDateStatus = new Date(results.subscription.startTime);
} else {
    result.startDateStatus = "Not specified"
}
 
if (results.subscription.endTime !== undefined) {
    result.endDateStatus = new Date(results.subscription.endTime);
} else {
    result.endDateStatus = "Not specified"
}