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
import org.bitrepository.pillar.referencepillar.ReferencePillar;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.service.LifeCycledService;

public class EmbeddedReferencePillar implements LifeCycledService {
    private final ReferencePillar pillar;

    public EmbeddedReferencePillar(Settings pillarSettings) {
        ReferencePillarDerbyDBTestUtils.createEmptyDatabases(pillarSettings);
        MessageBus messageBus =
                ProtocolComponentFactory.getInstance().getMessageBus(pillarSettings, new DummySecurityManager());
        pillar = new ReferencePillar(messageBus, pillarSettings);
    }

    @Override
    public void start() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void shutdown() {
        pillar.close();
    }
}
