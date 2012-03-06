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
import org.bitrepository.pillar.checksumpillar.ChecksumPillar;
import org.bitrepository.pillar.referencepillar.ReferencePillar;
import org.bitrepository.protocol.messagebus.MessageBus;

/**
 * Component factory for this module.
 */
public final class PillarComponentFactory {
    /** The singleton instance. */
    private static PillarComponentFactory instance;

    /**
     * Instantiation of this singleton.
     *
     * @return The singleton instance of this factory class.
     */
    public static synchronized PillarComponentFactory getInstance() {
        // ensure singleton.
        if(instance == null) {
            instance = new PillarComponentFactory();
        }
        return instance;
    }

    /** The characteristics for this module.*/
    private ModuleCharacteristics moduleCharacter;

    /**
     * Private constructor for initialization of the singleton.
     */
    private PillarComponentFactory() {
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
     * @param messageBus The messageBus for the reference pillar.
     * @param settings The settings for the pillar.
     * @return The reference requested pillar.
     */
    public ReferencePillar getReferencePillar(MessageBus messagebus, Settings settings) {
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(messagebus, "messagebus");
        
        return new ReferencePillar(messagebus, settings);
    }
    
    /**
     * Method for retrieving a checksum pillar.
     * @param messageBus The messageBus for the checksum pillar.
     * @param settings The settings for the pillar.
     * @return The reference requested checksum pillar.
     */
    public ChecksumPillar getChecksumPillar(MessageBus messagebus, Settings settings) {
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(messagebus, "messagebus");
        
        // TODO !!
        return new ChecksumPillar(messagebus, settings, null);
    }

}
