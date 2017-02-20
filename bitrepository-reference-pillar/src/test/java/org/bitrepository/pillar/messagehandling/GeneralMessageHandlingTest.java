/*
 * #%L
 * Bitrepository Reference Pillar
 * 
 * $Id: PutFileOnReferencePillarTest.java 589 2011-12-01 15:34:42Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/PutFileOnReferencePillarTest.java $
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
package org.bitrepository.pillar.messagehandling;

import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.pillar.MockedPillarTest;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.messagehandler.PillarMessageHandler;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.service.exception.RequestHandlerException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GeneralMessageHandlingTest extends MockedPillarTest {
    @Test( groups = {"regressiontest", "pillartest"})
    public void testPillarMessageHandler() throws Exception {
        addDescription("Test the handling of the PillarMessageHandler super-class.");
        addStep("Setup", "Should be OK.");
        MockRequestHandler mockRequestHandler = new MockRequestHandler(context, model);
        
        addStep("Test the pillar ID", "Should be Ok, with the id from settings, but not with another pillar id");
        mockRequestHandler.validatePillarID(getPillarID());
        try {
            mockRequestHandler.validatePillarID("asdfghjklæwetyguvpbmopijå.døtphstiøyizhdfvgnayegtxtæhjmdtuilsfm,s");
            Assert.fail("Should throw an IllegalArgumentException here!");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testPillarMessageHandlerValidateFileIDFormat() throws Exception {
        addDescription("Test the validation of file id formats of the PillarMessageHandler super-class.");
        addStep("Setup", "Should be OK.");
        MockRequestHandler mockRequestHandler = new MockRequestHandler(context, model);
        
        addStep("Test default valid file id", "Should be Ok");
        mockRequestHandler.validateFileIDFormat(DEFAULT_FILE_ID, collectionID);
        
        addStep("Test file id with folder structure", "Should be OK");
        mockRequestHandler.validateFileIDFormat("paht/" + DEFAULT_FILE_ID, collectionID);
        
        addStep("Test file id with a path which tries to use a parent directory", "should throw an exception");
        try {
            mockRequestHandler.validateFileIDFormat("../../OTHER_COLLECTION/folderDir/test.txt", collectionID);
            Assert.fail("Should throw an IllegalArgumentException here!");
        } catch (RequestHandlerException e) {
            // expected
        }

        addStep("Test file id with a path from root", "should throw an exception");
        try {
            mockRequestHandler.validateFileIDFormat("/usr/local/bin/execute.sh", collectionID);
            Assert.fail("Should throw an IllegalArgumentException here!");
        } catch (RequestHandlerException e) {
            // expected
        }
        
        addStep("Test a too long file id", "Should throw an exception");
        try {
            String fileId = "";
            for(int i = 0; i < 300; i++) {
                fileId += Integer.toString(i);
            }
            mockRequestHandler.validateFileIDFormat(fileId, collectionID);
            Assert.fail("Should throw an IllegalArgumentException here!");
        } catch (RequestHandlerException e) {
            // expected
        }
    }
    
    private class MockRequestHandler extends PillarMessageHandler<MessageRequest> {

        protected MockRequestHandler(MessageHandlerContext context, StorageModel model) {
            super(context, model);
        }

        @Override
        public Class<MessageRequest> getRequestClass() {
            return MessageRequest.class;
        }

        @Override
        public void processRequest(MessageRequest request, MessageContext messageContext) throws RequestHandlerException {}
        
        @Override
        public MessageResponse generateFailedResponse(MessageRequest request) {
            return null;
        }
        
        public void validatePillarID(String pillarID) throws RequestHandlerException {
            super.validatePillarId(pillarID);
        }
        
        public void validateFileIDFormat(String fileID, String collectionID) throws RequestHandlerException {
            super.validateFileIDFormat(fileID, collectionID);
        }
    }
}