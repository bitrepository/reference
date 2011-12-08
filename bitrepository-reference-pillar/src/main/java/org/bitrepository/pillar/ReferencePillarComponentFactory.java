/*
 * #%L
 * Bitrepository Integration
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
package org.bitrepository.pillar;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.ModuleCharacteristics;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBusManager;

/**
 * Component factory for this module.
 */
public final class ReferencePillarComponentFactory {
    /** The singleton instance. */
    private static ReferencePillarComponentFactory instance;

    /**
     * Instantiation of this singleton.
     *
     * @return The singleton instance of this factory class.
     */
    public static synchronized ReferencePillarComponentFactory getInstance() {
        // ensure singleton.
        if(instance == null) {
            instance = new ReferencePillarComponentFactory();
        }
        return instance;
    }

    /** The characteristics for this module.*/
    private ModuleCharacteristics moduleCharacter;

    /**
     * Private constructor for initialization of the singleton.
     */
    private ReferencePillarComponentFactory() {
        moduleCharacter = new ModuleCharacteristics("reference-pillar");
    }

    /**
     * Method for retrieving the characteristics for this module.
     * @return The characteristics for this module.
     */
    public ModuleCharacteristics getModuleCharacteristics() {
        return moduleCharacter;
    }

    /**
     * Method for retrieving a reference pillar.
     * @param settings The settings for the pillar.
     * @return The reference requested pillar.
     */
    public ReferencePillar getPillar(Settings settings) {
        ArgumentValidator.checkNotNull(settings, "settings");
        
        return new ReferencePillar(MessageBusManager.getMessageBus(settings), settings);
    }
}
