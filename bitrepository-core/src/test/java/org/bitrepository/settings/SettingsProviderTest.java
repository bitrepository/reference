/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.settings;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SettingsProviderTest {
    private static final String PATH_TO_TEST_SETTINGS = "settings/xml/bitrepository-devel";

    @Test(groups = {"regressiontest"})
    public void componentIDTest() {
        String myComponentID = "TestComponentID";
        SettingsProvider settingsLoader =
                new SettingsProvider(new XMLFileSettingsLoader(PATH_TO_TEST_SETTINGS), myComponentID);

        Settings settings = settingsLoader.getSettings();
        Assert.assertEquals(settings.getComponentID(), myComponentID);
    }

    @Test(groups = {"regressiontest"})
    public void reloadTest() {
        String myComponentID = "TestComponentID";
        SettingsProvider settingsLoader =
                new SettingsProvider(new XMLFileSettingsLoader(PATH_TO_TEST_SETTINGS), myComponentID);

        Settings settings = settingsLoader.getSettings();
        String originalCollectionID = settings.getCollectionID();

        String newCollectionID = "newCollectionID";
        settings.getRepositorySettings().getCollections().getCollection().get(0).setID(newCollectionID);
        Assert.assertEquals(settings.getRepositorySettings().getCollections().getCollection().get(0).getID(),
                newCollectionID);
        Assert.assertEquals(settings.getCollectionID(), newCollectionID);

        settingsLoader.reloadSettings();
        settings = settingsLoader.getSettings();
        Assert.assertEquals(settings.getRepositorySettings().getCollections().getCollection().get(0).getID(), originalCollectionID);
        Assert.assertEquals(settings.getCollectionID(), originalCollectionID);
    }
}
