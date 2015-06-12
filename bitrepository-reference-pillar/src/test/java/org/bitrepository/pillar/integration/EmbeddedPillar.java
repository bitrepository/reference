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


import java.io.File;
import java.util.Arrays;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.Pillar;
import org.bitrepository.pillar.PillarComponentFactory;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.messagebus.SimpleMessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.service.LifeCycledService;
import org.bitrepository.settings.referencesettings.CollectionDirs;
import org.bitrepository.settings.referencesettings.PillarType;

public class EmbeddedPillar implements LifeCycledService {
    private final Pillar pillar;

    private EmbeddedPillar(Pillar pillar) {
        this.pillar = pillar;
    }

    @Override
    public void start() {}

    @Override
    public void shutdown() {
        pillar.close();
    }

    public static EmbeddedPillar createReferencePillar(Settings pillarSettings) {
        MessageBus messageBus = initialize(pillarSettings);
        pillarSettings.getReferenceSettings().getPillarSettings().setPillarType(PillarType.FILE);
        return new EmbeddedPillar(PillarComponentFactory.getInstance().createPillar(pillarSettings, messageBus));
    }

    public static EmbeddedPillar createChecksumPillar(Settings pillarSettings) {
        MessageBus messageBus = initialize(pillarSettings);
        pillarSettings.getReferenceSettings().getPillarSettings().setPillarType(PillarType.CHECKSUM);
        return new EmbeddedPillar(PillarComponentFactory.getInstance().createPillar(pillarSettings, messageBus));
    }

    private static MessageBus initialize(Settings pillarSettings) {
        ReferencePillarDerbyDBTestUtils dbUtils = new ReferencePillarDerbyDBTestUtils(pillarSettings);
        dbUtils.createEmptyDatabases();
        for (CollectionDirs collectionDir : pillarSettings.getReferenceSettings().getPillarSettings().getCollectionDirs()) {
            for (String dir : collectionDir.getFileDirs() ) {
                FileUtils.deleteDirIfExists(new File(dir));
            }
        }
        return MessageBusManager.getMessageBus();
    }
}
