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
package org.bitrepository.pillar.messagefactories;

import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.message.ComponentTestMessageFactory;

import java.util.UUID;

public abstract class PillarTestMessageFactory {
    private final ComponentTestMessageFactory componentTestMessageFactory;
    private final String pillarDestinationID;
    protected final String pillarID;

    protected PillarTestMessageFactory(
            String collectionID, Settings testerSettings, String pillarID, String pillarDestinationID) {
        componentTestMessageFactory = new ComponentTestMessageFactory(collectionID, testerSettings);
        this.pillarDestinationID = pillarDestinationID;
        this.pillarID = pillarID;
    }

    protected void initializeIdentifyRequest(MessageRequest request) {
        componentTestMessageFactory.initializeMessageToComponentUnderTest(request);
    }

    protected void initializeOperationRequest(MessageRequest request) {
        componentTestMessageFactory.initializeMessageToComponentUnderTest(request);
        request.setDestination(pillarDestinationID);
        request.setTo(pillarID);
    }

    /**
     * Method for generating new correlation IDs.
     * @return A unique correlation id.
     */
    public String getNewCorrelationID() {
        return UUID.randomUUID().toString();
    }
}
