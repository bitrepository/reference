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
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.Pillar;
import org.bitrepository.pillar.PillarComponentFactory;
import org.bitrepository.service.LifeCycledService;
import org.bitrepository.settings.referencesettings.CollectionDirs;
import org.bitrepository.settings.referencesettings.PillarType;

import java.io.File;

import static org.bitrepository.protocol.IntegrationTest.messageBus;

public class EmbeddedPillar implements LifeCycledService {
    private final Pillar pillar;

    private EmbeddedPillar(Pillar pillar) {
        this.pillar = pillar;
    }

    @Override
    public void start() {
    }

    @Override
    public void shutdown() {
        pillar.close();
    }

    public static EmbeddedPillar createReferencePillar(Settings pillarSettings) {
        pillarSettings.getReferenceSettings().getPillarSettings().setPillarType(PillarType.FILE);
        initialize(pillarSettings);
        System.out.println("DEBUG: EmbeddedPillar creating FILE pillar with messageBus@" + System.identityHashCode(messageBus));
        return new EmbeddedPillar(PillarComponentFactory.getInstance().createPillar(pillarSettings, messageBus));
    }

    public static EmbeddedPillar createChecksumPillar(Settings pillarSettings) {
        pillarSettings.getReferenceSettings().getPillarSettings().setPillarType(PillarType.CHECKSUM);
        initialize(pillarSettings);
        System.out.println("DEBUG: EmbeddedPillar creating CHECKSUM pillar with messageBus@" + System.identityHashCode(messageBus));
        return new EmbeddedPillar(PillarComponentFactory.getInstance().createPillar(pillarSettings, messageBus));
    }

    private static void initialize(Settings pillarSettings) {
        ReferencePillarDerbyDBTestUtils dbUtils = new ReferencePillarDerbyDBTestUtils(pillarSettings);
        dbUtils.createEmptyDatabases();
        for (CollectionDirs collectionDir : pillarSettings.getReferenceSettings().getPillarSettings().getCollectionDirs()) {
            for (String dir : collectionDir.getFileDirs()) {
                FileUtils.deleteDirIfExists(new File(dir));
            }
        }
    }
}
