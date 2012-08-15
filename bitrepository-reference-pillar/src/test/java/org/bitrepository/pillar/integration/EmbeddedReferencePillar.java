package org.bitrepository.pillar.integration;
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


import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.pillar.PillarSettingsProvider;
import org.bitrepository.pillar.referencepillar.ReferencePillar;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;

public class EmbeddedReferencePillar {

    public EmbeddedReferencePillar(String pathToReferencePillarSettings, String pillarID) {

        PillarSettingsProvider settingsLoader =
                new PillarSettingsProvider(new XMLFileSettingsLoader(pathToReferencePillarSettings), pillarID);
        Settings settings = settingsLoader.getSettings();
        ReferencePillarDerbyDBTestUtils.createEmptyDatabases(settings);
        MessageBus messageBus =
                ProtocolComponentFactory.getInstance().getMessageBus(settings, new DummySecurityManager());
        new ReferencePillar(messageBus, settings);
    }
}
