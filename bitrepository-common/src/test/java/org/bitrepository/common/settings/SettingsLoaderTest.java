/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.common.settings;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

public class SettingsLoaderTest extends ExtendedTestCase{
    private static final String PATH_TO_SETTINGS = "settings/xml";

    //    @Test(groups = { "regressiontest" })
    public void testLocalCollectionSettingsLoading() throws Exception {
        SettingsProvider settingsLoader = 
                new SettingsProvider(new XMLFileSettingsLoader(PATH_TO_SETTINGS));

        Settings settings = settingsLoader.getSettings("bitrepository-local");
    }

    @Test(groups = { "regressiontest" })
    public void cd() throws Exception {
        SettingsProvider settingsLoader = 
                new SettingsProvider(new XMLFileSettingsLoader(PATH_TO_SETTINGS));

        Settings settings = settingsLoader.getSettings("bitrepository-devel");
    }

    //    @Test(groups = { "regressiontest" })
    public void testIntegrationCollectionSettingsLoading() throws Exception {
        SettingsProvider settingsLoader = 
                new SettingsProvider(new XMLFileSettingsLoader(PATH_TO_SETTINGS));

        Settings settings = settingsLoader.getSettings("bitrepository-integration");        
    }
}
