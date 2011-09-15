/*
 * #%L
 * bitrepository-access-client
 * *
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
package org.bitrepository.pillar.messagehandler;

import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.collection.settings.standardsettings.Settings;
import org.bitrepository.pillar.ReferenceArchive;
import org.bitrepository.protocol.messagebus.MessageBus;

/**
 * Class for performing the GetChecksums operation for this pillar.
 * TODO handle error scenarios.
 */
public class GetChecksumsRequestHandler extends PillarMessageHandler<GetChecksumsRequest> {
    /**
     * Constructor.
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    public GetChecksumsRequestHandler(Settings settings, MessageBus messageBus,
            AlarmDispatcher alarmDispatcher, ReferenceArchive referenceArchive) {
        super(settings, messageBus, alarmDispatcher, referenceArchive);
    }

    
    /**
     * Handles the messages for the GetChecksums operation.
     * TODO perhaps synchronisation?
     * @param message The GetChecksumsRequest message to handle.
     */
    public void handleMessage(GetChecksumsRequest message) {
        // TODO handle!
        throw new IllegalArgumentException("Not Implemented");
    }
}
