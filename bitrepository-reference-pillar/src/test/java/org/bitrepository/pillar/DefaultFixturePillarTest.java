/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id: DefaultFixturePillarTest.java 452 2011-11-10 09:59:11Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/DefaultFixturePillarTest.java $
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
package org.bitrepository.pillar;

import org.bitrepository.client.MessageReceiver;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

/**
 * Contains the generic parts for pillar tests integrating to the message bus. 
 * Mostly copied from DefaultFixtureClientTest...
 */
public abstract class DefaultFixturePillarTest extends IntegrationTest {
    protected static final String DEFAULT_FILE_ID = ClientTestMessageFactory.FILE_ID_DEFAULT;

    protected static String pillarDestinationId;

    protected Settings clientSettings;
    protected String clientDestinationId;
    protected MessageReceiver clientTopic;
    
    /** Indicated whether reference pillars should be started should be started and used. Note that mockup pillars 
     * should be used in this case, e.g. the useMockupPillar() call should return false. */ 
    public boolean useEmbeddedPillar() {
        return System.getProperty("useEmbeddedPillar", "false").equals("true");
    }

    @Override
    protected void setupSettings() {
        super.setupSettings();
        clientSettings = TestSettingsProvider.reloadSettings("TestClientForPillarTest");
        clientSettings.getCollectionSettings().getProtocolSettings().setCollectionDestination(collectionDestinationID);

        componentSettings.getReferenceSettings().getPillarSettings().setPillarID(getPillarID());
    }

    @Override
    protected void initializeMessageBusListeners() {
        super.initializeMessageBusListeners();
        clientTopic = new MessageReceiver("Client topic receiver", testEventManager);
        clientDestinationId = clientSettings.getReceiverDestinationID();
        messageBus.addListener(clientDestinationId, clientTopic.getMessageListener());

        pillarDestinationId = componentSettings.getReceiverDestinationID();
    }

    @Override
    protected void teardownMessageBusListeners() {
        messageBus.removeListener(clientDestinationId, clientTopic.getMessageListener());
        super.teardownMessageBusListeners();
    }

    protected String getPillarID() {
        return getComponentID();
    }
}
