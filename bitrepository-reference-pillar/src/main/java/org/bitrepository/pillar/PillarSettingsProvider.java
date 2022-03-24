/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar;

import org.bitrepository.common.settings.SettingsLoader;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.settings.referencesettings.ReferenceSettings;

public class PillarSettingsProvider extends SettingsProvider {
    private final String pillarID;

    /**
     * Creates a {@link SettingsProvider} which will use the provided {@link SettingsLoader} for loading the
     * settings.
     *
     * @param settingsReader Use for loading the settings.
     * @param pillarID       Optional pillarID if more than one reference pillar in the collection. If null the
     *                       pillarID from the first pillar settings section in the reference settings wil be used.
     */
    public PillarSettingsProvider(SettingsLoader settingsReader, String pillarID) {
        super(settingsReader, null);
        this.pillarID = pillarID;
    }

    protected String getComponentID(ReferenceSettings referenceSettings) {
        if (pillarID == null) {
            return referenceSettings.getPillarSettings().getPillarID();
        } else {
            return pillarID;
        }
    }
}
