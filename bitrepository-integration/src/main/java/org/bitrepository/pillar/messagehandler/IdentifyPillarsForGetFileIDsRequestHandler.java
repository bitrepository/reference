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

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.ReferenceArchive;
import org.bitrepository.protocol.messagebus.MessageBus;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetFileIDs operation.
 * TODO handle error scenarios.
 */
public class IdentifyPillarsForGetFileIDsRequestHandler extends PillarMessageHandler<IdentifyPillarsForGetFileIDsRequest> {
    /**
     * Constructor.
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    public IdentifyPillarsForGetFileIDsRequestHandler(Settings settings, MessageBus messageBus,
            AlarmDispatcher alarmDispatcher, ReferenceArchive referenceArchive) {
        super(settings, messageBus, alarmDispatcher, referenceArchive);
    }
    
    /**
     * Handles the identification messages for the GetFileIDs operation.
     * TODO perhaps synchronisation?
     * @param message The IdentifyPillarsForGetFileIDsRequest message to handle.
     */
    public void handleMessage(IdentifyPillarsForGetFileIDsRequest message) {
        // TODO handle!
        throw new IllegalArgumentException("Not Implemented");
    }
}
