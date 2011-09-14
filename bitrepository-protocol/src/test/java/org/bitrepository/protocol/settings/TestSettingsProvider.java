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

public class TestSettingsProvider implements SettingsReader {
    public final static String DEFAULT_SETTINGS = "Default-settings";

    /** Loads the default test settings 
     * @param <T>*/
    public <T> T loadDefaultSettings(Class<T> configurationClass) {
        return loadSettings(DEFAULT_SETTINGS, configurationClass);
    }

    @Override
    public <T> T loadSettings(String collectionID, Class<T> configurationClass) {
        // TODO Auto-generated method stub
        return null;
    }
}
