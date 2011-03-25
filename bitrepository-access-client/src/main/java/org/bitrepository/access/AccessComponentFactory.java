/*
 * #%L
 * bitrepository-access-client
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
package org.bitrepository.access;

import org.bitrepository.common.ModuleCharacteristics;

/**
 * Factory class for the access module. 
 * Instantiates the instances of the interfaces within this module.
 */
public class AccessComponentFactory {
    /** The singleton instance. */
    private static AccessComponentFactory instance;
    
    /**
     * Instantiation of this singleton.
     * 
     * @return The singleton instance of this factory class.
     */
    public static AccessComponentFactory getInstance() {
        // ensure singleton.
        if(instance == null) {
            instance = new AccessComponentFactory();
        }
        return instance;
    }
    
    /** The characteristics for this module.*/
    private ModuleCharacteristics moduleCharacter;
    
    /**
     * Private constructor for initialisation of the singleton.
     */
    private AccessComponentFactory() { 
        moduleCharacter = new ModuleCharacteristics("access-client");
    }
    
    /**
     * Method for retrieving the characteristics for this module.
     * @return The characteristics for this module.
     */
    public ModuleCharacteristics getModuleCharacteristics() {
        return moduleCharacter;
    }
    
    /**
     * Method for instantiating the GetFileClient defined in the configuration.
     * TODO use the configuration!!!!!
     * 
     * @return The GetFileClient.
     */
    public GetFileClientExternalAPI retrieveGetFileClient() {
        // TODO use the configurations instead!
        return new SimpleGetFileClient();
    }
}
