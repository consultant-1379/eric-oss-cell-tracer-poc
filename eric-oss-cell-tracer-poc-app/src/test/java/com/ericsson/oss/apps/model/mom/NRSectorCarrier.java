/*******************************************************************************
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
 ******************************************************************************/
package com.ericsson.oss.apps.model.mom;

import com.ericsson.oss.apps.ncmp.model.ManagedObject;
import com.ericsson.oss.apps.ncmp.model.Toggle;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class NRSectorCarrier extends ManagedObject {

    @Serial
    private static final long serialVersionUID = 4177270645822367105L;

    public NRSectorCarrier(String fdn) {
        super(fdn);
    }

    @JsonProperty(value = "nRSectorCarrierId", access = JsonProperty.Access.WRITE_ONLY)
    private String nRSectorCarrierId;
    private Toggle operationalState;

    @Override
    public String getId() {
        return nRSectorCarrierId;
    }

}

