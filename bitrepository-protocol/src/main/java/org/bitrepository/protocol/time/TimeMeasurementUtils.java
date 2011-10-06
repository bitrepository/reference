package org.bitrepository.protocol.time;

import java.math.BigInteger;

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE.TimeMeasureUnit;

/**
 * Provides helper method for accessing {@link TimeMeasurementTYPE} objects. 
 */
public class TimeMeasurementUtils {

    /**
     * Generates a TimeMeasureTYPE object based on a milliseconds value.
     * @param milliseconds The time measure in milliseconds.
     * @return A corresponding <code>TimeMeasureTYPE</code> object.
     */
    public static TimeMeasureTYPE getTimeMeasurementFromMiliseconds(BigInteger milliseconds) {
        TimeMeasureTYPE timeMeasure = new TimeMeasureTYPE();
        timeMeasure.setTimeMeasureValue(milliseconds);
        timeMeasure.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);
        return timeMeasure; 
    }
}
