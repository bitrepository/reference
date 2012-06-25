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


/**
 * Helper class for easy access to the settings located in the <code>settings/xml</code> dir.
 */
public class TestSettingsProvider {
    /** 
     * Returns the settings for the collection defined by the COLLECTIONID_PROPERTY system variable if defined. If 
     * undefined the DEVELOPMENT_ENVIRONMENT settings will be loaded.
     */
    public static Settings getSettings(String componentID) {
        return createSettingsProvider(componentID).getSettings();
    }
    
    /** 
     * Reloads the settings from disk.
     */
    public static Settings reloadSettings(String componentID) {
        createSettingsProvider(componentID).reloadSettings();
        return getSettings(componentID);
    }

    private static SettingsProvider createSettingsProvider(String componentID) {
        return new SettingsProvider(new XMLFileSettingsLoader("settings/xml/bitrepository-devel"), componentID);
    }
}
