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


import com.ericsson.oss.apps.ncmp.model.AdministrativeState;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;

import java.io.Serial;

@Getter
@Setter
@Entity
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public class NRCellDU extends NRCell {

    @Serial
    private static final long serialVersionUID = -3671129854314179036L;

    public NRCellDU(String fdn) {
        super(fdn);
    }

    private AdministrativeState administrativeState;
    @JsonProperty("nCI")
    private Long nCI;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "ME_FDN", referencedColumnName = "ME_FDN")),
            @JoinColumnOrFormula(formula = @JoinFormula(value = "PARENT_RES_REF", referencedColumnName = "RES_REF"))
    })
    private GNBDUFunction gNodeB;
}
