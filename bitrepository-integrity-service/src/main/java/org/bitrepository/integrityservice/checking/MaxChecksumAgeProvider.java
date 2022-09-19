/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.integrityservice.checking;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.XmlUtils;
import org.bitrepository.settings.referencesettings.MaxChecksumAgeForPillar;
import org.bitrepository.settings.referencesettings.ObsoleteChecksumSettings;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.time.Duration;
import java.util.Objects;

/**
 * Provide easy access to the MaxChecksumAge for individual pillars.
 */
public class MaxChecksumAgeProvider {
    private final Duration defaultMaxAge;
    private final ObsoleteChecksumSettings settings;

    public MaxChecksumAgeProvider(Duration defaultMaxAge, ObsoleteChecksumSettings settings) {
        this.defaultMaxAge = Objects.requireNonNull(defaultMaxAge, "defaultMaxAge");
        this.settings = settings;
    }

    /**
     * Returns the MaxChecksumAge for a pillar as:
     * <ol>
     *     <li>The ObsoleteChecksumSettings.getMaxChecksumAgeForPillar setting if defined for the indicated
     *     pillar.</li>
     *     <li>The ObsoleteChecksumSettings.getDefaultMaxChecksumAge setting if defined</li>
     *     <li>The defaultMaxAge.</li>
     * </ol>
     *
     * @param pillarID the ID of the pillar
     * @return The MaxChecksumAge for the indicated pillar.
     */
    public Duration getMaxChecksumAge(String pillarID) {
        ArgumentValidator.checkNotNull(pillarID, "pillarID");
        if (settings != null) {
            if (settings.getMaxChecksumAgeForPillar() != null) {
                for (MaxChecksumAgeForPillar maxChecksumAgeForPillar : settings.getMaxChecksumAgeForPillar()) {
                    if (pillarID.equals(maxChecksumAgeForPillar.getPillarID())) {
                        javax.xml.datatype.Duration maxChecksumAge = maxChecksumAgeForPillar.getMaxChecksumAge();
                        return XmlUtils.xmlDurationToDuration(maxChecksumAge);
                    }
                }
            }
            javax.xml.datatype.Duration defaultMaxChecksumAge = settings.getDefaultMaxChecksumAge();
            if (defaultMaxChecksumAge != null) {
                return XmlUtils.xmlDurationToDuration(defaultMaxChecksumAge);
            }
        }
        return defaultMaxAge;
    }

    /**
     * Provides one line construction of MaxChecksumAgeForPillar objects.
     *
     * @param pillarID the pillar ID to set
     * @param value    the max checksum age to set
     * @return a new MaxCheckAgeForPillar Object
     */
    public static MaxChecksumAgeForPillar createMaxChecksumAgeForPillar(String pillarID, long value) {
        MaxChecksumAgeForPillar maxChecksumAgeForPillar = new MaxChecksumAgeForPillar();
        maxChecksumAgeForPillar.setPillarID(pillarID);
        try {
            maxChecksumAgeForPillar.setMaxChecksumAge(DatatypeFactory.newInstance().newDuration(value));
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("createMaxChecksumAgeForPillar(): creating datatype factory failed: ", e);
        }
        return maxChecksumAgeForPillar;
    }
}
