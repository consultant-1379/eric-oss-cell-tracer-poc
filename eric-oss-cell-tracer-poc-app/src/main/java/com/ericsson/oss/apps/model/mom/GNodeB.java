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
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;

@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
public abstract class GNodeB extends ManagedObject {

    @Serial
    private static final long serialVersionUID = 5362568752262201741L;

    @JsonProperty(value = "gNBId")
    private Long gNBId;
    @JsonProperty(value = "gNBIdLength")
    private Integer gNBIdLength;
    @Embedded
    @JsonProperty(value = "pLMNId")
    private PLMNId pLMNId;

    GNodeB(String fdn) {
        super(fdn);
    }

}
