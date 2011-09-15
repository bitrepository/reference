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
package org.bitrepository.protocol.settings;

import org.bitrepository.protocol.bitrepositorycollection.MutableCollectionSettings;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

public class CollectionSettingsLoaderTest extends ExtendedTestCase{
    private static final String PATH_TO_SETTINGS = "settings/xml";

//    @Test(groups = { "regressiontest" })
    public void testLocalCollectionSettingsLoading() throws Exception {
        CollectionSettingsLoader settingsLoader = 
            new CollectionSettingsLoader(new XMLFileSettingsLoader(PATH_TO_SETTINGS));
        
        MutableCollectionSettings collectionSettings = new MutableCollectionSettings();
        
        settingsLoader.addCollectionSettings("bitrepository-local", collectionSettings);
    }
    
    @Test(groups = { "regressiontest" })
    public void testDevelCollectionSettingsLoading() throws Exception {
        CollectionSettingsLoader settingsLoader = 
            new CollectionSettingsLoader(new XMLFileSettingsLoader(PATH_TO_SETTINGS));
        
        MutableCollectionSettings collectionSettings = new MutableCollectionSettings();
        
        settingsLoader.addCollectionSettings("bitrepository-devel", collectionSettings);
    }
    
//    @Test(groups = { "regressiontest" })
    public void testIntegrationCollectionSettingsLoading() throws Exception {
        CollectionSettingsLoader settingsLoader = 
            new CollectionSettingsLoader(new XMLFileSettingsLoader(PATH_TO_SETTINGS));
        
        MutableCollectionSettings collectionSettings = new MutableCollectionSettings();
        
        settingsLoader.addCollectionSettings("bitrepository-integration", collectionSettings);
    }
}
