/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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

import org.bitrepository.collection.settings.standardsettings.Settings;
import org.bitrepository.pillar.messagehandler.PillarMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reference pillar. This very simply starts the PillarMediator, which handles all the communications.
 */
public class ReferencePillar {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    public ReferencePillar(MessageBus mBus, Settings settings) {
        log.info("Starting the reference pillar!");
        
        ReferencePillarMessageFactory msgFactory = new ReferencePillarMessageFactory(settings);
        ReferenceArchive archive = new ReferenceArchive(settings.getPillar().getFileDir());
        PillarMediator mediator = new PillarMediator(mBus, settings, archive, msgFactory);
        log.info("ReferencePillar started!");
    }
}
