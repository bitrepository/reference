/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id: AccessMessageHandler.java 249 2011-08-02 11:05:51Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-integration/src/main/java/org/bitrepository/pillar/messagehandler/AccessMessageHandler.java $
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

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetChecksums operation.
 * TODO handle error scenarios.
 */
public class GetChecksumsIdentificationMessageHandler 
        extends PillarMessageHandler<IdentifyPillarsForGetChecksumsRequest> {
    /**
     * Constructor.
     * @param mediator The mediator for this pillar.
     */
    public GetChecksumsIdentificationMessageHandler(PillarMediator mediator) {
        super(mediator);
    }

    /**
     * Handles the identification messages for the GetChecksums operation.
     * TODO perhaps synchronisation?
     * @param message The IdentifyPillarsForGetChecksumsRequest message to handle.
     */
    public void handleMessage(IdentifyPillarsForGetChecksumsRequest message) {
        // TODO handle!
        throw new IllegalArgumentException("Not Implemented");
    }
}
