/*
 * #%L
 * Bitrepository Reference Pillar
 *
 * $Id: PutFileOnReferencePillarTest.java 589 2011-12-01 15:34:42Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org
 * /bitrepository/pillar/PutFileOnReferencePillarTest.java $
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

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.pillar.MockedPillarTest;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.messagehandler.PillarMessageHandler;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.service.exception.RequestHandlerException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;

public class GeneralMessageHandlingTest extends MockedPillarTest {
    MockRequestHandler requestHandler;

    @BeforeMethod(alwaysRun = true)
    public void setup() {
        this.requestHandler = new MockRequestHandler(context, model);
    }

    @Test(groups = {"regressiontest", "pillartest"})
    public void testPillarMessageHandler() throws Exception {
        addDescription("Test the handling of the PillarMessageHandler super-class.");
        addStep("Setup", "Should be OK.");

        addStep("Test the pillar ID", "Should be Ok, with the id from settings, but not with another pillar id");
        requestHandler.validatePillarID(getPillarID());
        try {
            requestHandler.validatePillarID("asdfghjklæwetyguvpbmopijå.døtphstiøyizhdfvgnayegtxtæhjmdtuilsfm,s");
            Assert.fail("Should throw an IllegalArgumentException here!");
        } catch (IllegalArgumentException e) {
            // expected
        }

        addStep("Testing verification of FileID existence with null value.", "Should be okay with FileID being null");
        FileIDs nullFileIDs = new FileIDs();
        nullFileIDs.setFileID(null);
        requestHandler.verifyFileIDExistence(nullFileIDs, collectionID);

        addStep("Testing verification of a non-existing FileID.", "Should throw an exception as the FileID doesn't exist.");
        try {
            FileIDs wrongIDs = new FileIDs();
            wrongIDs.setFileID("NonExistingFileID");
            requestHandler.verifyFileIDExistence(wrongIDs, collectionID);
            Assert.fail("Should throw a RequestHandlerException.");
        } catch (RequestHandlerException e) {
            // expected
        }

        addStep("Testing verification of FileID existence with correct values.", "Should be okay as pillar is mocked.");
        String FILE_ID = DEFAULT_FILE_ID + testMethodName;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);
        doAnswer(invocation -> true).when(model).hasFileID(eq(FILE_ID), anyString());
        doAnswer(invocation -> settingsForCUT.getComponentID()).when(model).getPillarID();

        requestHandler.verifyFileIDExistence(fileids, collectionID);
    }

    @Test(groups = {"regressiontest", "pillartest"})
    public void testPillarMessageHandlerValidateFileIDFormatDefaultFileId() throws Exception {
        addDescription("Test the validation of file id formats of the PillarMessageHandler super-class on the default file id");
        requestHandler.validateFileIDFormat(DEFAULT_FILE_ID);
    }

    @Test(groups = {"regressiontest", "pillartest"})
    public void testPillarMessageHandlerValidateFileIDFormatFolderFileId() throws Exception {
        addDescription("Test the validation of file id formats of the PillarMessageHandler super-class on a file id with directory path");
        requestHandler.validateFileIDFormat("path/" + DEFAULT_FILE_ID);
    }

    @Test(groups = {"regressiontest", "pillartest"}, expectedExceptions = RequestHandlerException.class)
    public void testPillarMessageHandlerValidateFileIDFormatParentFolderFileId() throws Exception {
        addDescription(
                "Test the validation of file id formats of the PillarMessageHandler super-class on a file id containing path to a parent " +
                        "directory");
        requestHandler.validateFileIDFormat("../../OTHER_COLLECTION/folderDir/test.txt");
    }

    @Test(groups = {"regressiontest", "pillartest"}, expectedExceptions = RequestHandlerException.class)
    public void testPillarMessageHandlerValidateFileIDFormatRootPathFileId() throws Exception {
        addDescription(
                "Test the validation of file id formats of the PillarMessageHandler super-class on a file id containing path from the " +
                        "root folder");
        requestHandler.validateFileIDFormat("/usr/local/bin/execute.sh");
    }

    @Test(groups = {"regressiontest", "pillartest"}, expectedExceptions = RequestHandlerException.class)
    public void testPillarMessageHandlerValidateFileIDFormatSubFolderToParentFolderFileId() throws Exception {
        addDescription(
                "Test the validation of file id formats of the PillarMessageHandler super-class on a file id containing path to a parent " +
                        "directory, but starting with a sub-folder");
        requestHandler.validateFileIDFormat("OTHER_COLLECTION/../../folderDir/test.txt");
    }

    @Test(groups = {"regressiontest", "pillartest"}, expectedExceptions = RequestHandlerException.class)
    public void testPillarMessageHandlerValidateFileIDFormatEnvHomePathFileId() throws Exception {
        addDescription(
                "Test the validation of file id formats of the PillarMessageHandler super-class on a file id containing path relative " +
                        "paths from the environment variable home folder");
        requestHandler.validateFileIDFormat("$HOME/bin/execute.sh");
    }

    @Test(groups = {"regressiontest", "pillartest"}, expectedExceptions = RequestHandlerException.class)
    public void testPillarMessageHandlerValidateFileIDFormatTildeHomePathFileId() throws Exception {
        addDescription(
                "Test the validation of file id formats of the PillarMessageHandler super-class on a file id containing path relative " +
                        "paths from the tilde home folder");
        requestHandler.validateFileIDFormat("~/bin/execute.sh");
    }

    @Test(groups = {"regressiontest", "pillartest"}, expectedExceptions = RequestHandlerException.class)
    public void testPillarMessageHandlerValidateFileIDFormatTooLong() throws Exception {
        addDescription(
                "Test the validation of file id formats of the PillarMessageHandler super-class on a file id which has more characters " +
                        "than required");
        StringBuilder fileId = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            fileId.append(i);
        }
        requestHandler.validateFileIDFormat(fileId.toString());
    }

    private static class MockRequestHandler extends PillarMessageHandler<MessageRequest> {
        protected MockRequestHandler(MessageHandlerContext context, StorageModel model) {
            super(context, model);
        }

        @Override
        public Class<MessageRequest> getRequestClass() {
            return MessageRequest.class;
        }

        @Override
        public void processRequest(MessageRequest request, MessageContext messageContext) {}

        @Override
        public MessageResponse generateFailedResponse(MessageRequest request) {
            return null;
        }

        @Override
        public void verifyFileIDExistence(FileIDs fileIDs, String collectionID) throws RequestHandlerException {
            super.verifyFileIDExistence(fileIDs, collectionID);
        }

        public void validatePillarID(String pillarID) {
            super.validatePillarId(pillarID);
        }

        public void validateFileIDFormat(String fileID) throws RequestHandlerException {
            super.validateFileIDFormat(fileID);
        }
    }
}