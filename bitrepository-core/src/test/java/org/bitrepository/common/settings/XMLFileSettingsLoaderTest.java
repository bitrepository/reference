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

import org.bitrepository.TestGroups;
import org.bitrepository.settings.repositorysettings.RepositorySettings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class XMLFileSettingsLoaderTest{
    private static final String PATH_TO_SETTINGS = "settings/xml/bitrepository-devel";
    
    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void testCollectionSettingsLoading() throws Exception {
        SettingsLoader settingsLoader = new XMLFileSettingsLoader(PATH_TO_SETTINGS);
        
        RepositorySettings repositorySettings = settingsLoader.loadSettings(RepositorySettings.class);
        Assertions.assertNotNull(repositorySettings, "RepositorySettings");
    }
}
