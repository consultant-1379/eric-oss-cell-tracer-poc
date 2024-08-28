#!/bin/bash -ex
#
# COPYRIGHT Ericsson 2023
#
#
#
# The copyright to the computer program(s) herein is the property of
#
# Ericsson Inc. The programs may be used and/or copied only with written
#
# permission from Ericsson Inc. or in accordance with the terms and
#
# conditions stipulated in the agreement/contract under which the
#
# program(s) have been supplied.
#



echo "========== Variables -> TRUST_STORE_LOCATION='${TRUST_STORE_LOCATION}' or CA_CERT_LOCATION='${CA_CERT_LOCATION}' or KAFKA_CERT_LOCATION='${KAFKA_CERT_LOCATION}' =========="

if [ -n "$TRUST_STORE_LOCATION" ] && [ -n "$CA_CERT_LOCATION" ] && [ -n "$KAFKA_CERT_LOCATION" ]; then
    if [ -f "${TRUST_STORE_LOCATION}/truststore.jks" ]; then
        echo "========== Removing the existing truststore =========="
        rm -f "${TRUST_STORE_LOCATION}/truststore.jks"
        echo "========== Existing truststore removed =========="
    fi
    echo "========== Generating JKS files =========="
    keytool -importcert -file "${KAFKA_CERT_LOCATION}/tls.crt" -alias kafkaCert -keystore "${TRUST_STORE_LOCATION}/truststore.jks" -storepass "${TRUST_STORE_PASSWORD}" -noprompt
    keytool -importcert -file "${CA_CERT_LOCATION}/tls.crt" -alias iamCert -keystore "${TRUST_STORE_LOCATION}/truststore.jks" -storepass "${TRUST_STORE_PASSWORD}" -noprompt
    echo "========== JKS file generated under '${TRUST_STORE_LOCATION}'/truststore.jks =========="
else
    echo "========== One or more of the following variables are empty: TRUST_STORE_LOCATION='${TRUST_STORE_LOCATION}', CA_CERT_LOCATION='${CA_CERT_LOCATION}', KAFKA_CERT_LOCATION='${KAFKA_CERT_LOCATION}' =========="
fi

exec "$@"