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
package com.ericsson.oss.apps.generator;

import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;

import java.io.Serial;
import java.security.SecureRandom;

import static org.jeasy.random.FieldPredicates.named;

public class RandomEventGenerator extends EasyRandom {

    @Serial
    private static final long serialVersionUID = 1;

    private static EasyRandomParameters getRandomParameters(RandomEventFactory eventFactory, int seed) {
        SecureRandom rn = new SecureRandom();
        return new EasyRandomParameters()
                .seed(seed)
                .objectFactory(eventFactory)
                .randomize(named("pmEventGroupVersion_")
                        .or(named("pmEventCommonVersion_"))
                        .or(named("etcmVersion_")), () -> rn.nextInt(4) + 1)
                .randomize(named("pmEventCorrectionVersion_")
                        .or(named("etcmCorrectionVersion_")), () -> rn.nextInt(101));
    }

    public RandomEventGenerator(RandomEventFactory eventFactory, int seed) {
        super(getRandomParameters(eventFactory, seed));
    }

}
