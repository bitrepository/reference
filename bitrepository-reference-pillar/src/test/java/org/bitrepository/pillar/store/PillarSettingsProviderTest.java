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
package org.bitrepository.pillar.store;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.pillar.PillarSettingsProvider;
import org.testng.Assert;
import org.testng.annotations.Test;

public class PillarSettingsProviderTest {

    private static final String PATH_TO_TEST_SETTINGS = "settings/xml/bitrepository-devel";

    @Test(groups = {"regressiontest"})
    public void componentIDTest() {
        SettingsProvider settingsLoader =
                new PillarSettingsProvider(new XMLFileSettingsLoader(PATH_TO_TEST_SETTINGS),
                        null);
        Settings settings = settingsLoader.getSettings();
        Assert.assertEquals(
                settings.getReferenceSettings().getPillarSettings().getPillarID(), settings.getComponentID());

        String componentID = "testPillarID";
        settingsLoader =
                new PillarSettingsProvider(new XMLFileSettingsLoader(PATH_TO_TEST_SETTINGS),
                        "testPillarID");
        settings = settingsLoader.getSettings();
        Assert.assertEquals(
                componentID, settings.getComponentID());
    }
}
