package org.bitrepository.pillar.integration.func;
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
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.putfile.BlockingPutFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.pillar.integration.PillarIntegrationTest;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.testng.annotations.BeforeSuite;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

/**
 * The parent class for pillar acceptance tests. The tests can be run in a multi pillar collection has the tests will
 * ignore responses from other pillars.
 */
public class PillarFunctionTest extends PillarIntegrationTest {
    /** Used for receiving responses from the pillar */
    protected MessageReceiver clientReceiver;
    protected static URL FILE_ADDRESS;

    /**
     * Adds a client topic listener.
     *
     * Will also add a pillar filter to the client and alarm receivers, so responses from irrelevant components are
     * ignored.
     */
    @Override
    protected void initializeMessageBusListeners() {
        super.initializeMessageBusListeners();
        clientReceiver = new MessageReceiver("Client topic receiver", testEventManager);
        messageBus.addListener(componentSettings.getReceiverDestinationID(), clientReceiver.getMessageListener());

        Collection<String> pillarFilter = Arrays.asList(testConfiguration.getPillarUnderTestID());
        clientReceiver.setFromFilter(pillarFilter);
        alarmReceiver.setFromFilter(pillarFilter);
    }

    public void putDefaultFile() throws Exception {
        try {
            FILE_ADDRESS = httpServer.getURL(TestFileHelper.DEFAULT_FILE_ID);
        } catch (MalformedURLException e) { // Doesn't happen.
        }
        BlockingPutFileClient putFileClient = new BlockingPutFileClient(ModifyComponentFactory.getInstance().retrievePutClient(
                componentSettings, createSecurityManager(), componentSettings.getComponentID()));
        putFileClient.putFile(FILE_ADDRESS, TestFileHelper.DEFAULT_FILE_ID, 10L, null, null, null, null);
    }

}
