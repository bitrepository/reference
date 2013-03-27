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
import org.bitrepository.pillar.Pillar;
import org.bitrepository.pillar.cache.ChecksumDAO;
import org.bitrepository.pillar.checksumpillar.ChecksumPillar;
import org.bitrepository.pillar.referencepillar.ReferencePillar;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.service.LifeCycledService;

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
        return new EmbeddedPillar(new ReferencePillar(messageBus, pillarSettings));
    }

    public static EmbeddedPillar createChecksumPillar(Settings pillarSettings) {
        MessageBus messageBus = initialize(pillarSettings);
        return new EmbeddedPillar(new ChecksumPillar(messageBus, pillarSettings,
                new ChecksumDAO(pillarSettings)));
    }

    private static MessageBus initialize(Settings pillarSettings) {
        ReferencePillarDerbyDBTestUtils dbUtils = new ReferencePillarDerbyDBTestUtils(pillarSettings);
        dbUtils.createEmptyDatabases();
        return ProtocolComponentFactory.getInstance().getMessageBus(pillarSettings, new DummySecurityManager());
    }
}
