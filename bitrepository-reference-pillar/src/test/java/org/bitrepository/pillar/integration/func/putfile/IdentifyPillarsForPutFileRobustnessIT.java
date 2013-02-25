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
package org.bitrepository.pillar.integration.func.putfile;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.pillar.integration.func.PillarRobustnessTest;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.testng.annotations.BeforeMethod;

public class IdentifyPillarsForPutFileRobustnessIT extends PillarRobustnessTest {
    protected PutFileMessageFactory msgFactory;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        msgFactory = new PutFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createIdentifyPillarsForPutFileRequest(
                DEFAULT_FILE_ID, 0L);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(IdentifyPillarsForPutFileResponse.class);
    }
}
