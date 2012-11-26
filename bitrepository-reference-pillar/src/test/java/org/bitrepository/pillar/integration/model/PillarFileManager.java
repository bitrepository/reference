/*
 * #%L
 * Bitrepository Integrity Service
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

package org.bitrepository.pillar.integration.model;

import java.util.List;
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.common.exceptions.OperationFailedException;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.integration.ClientProvider;
import org.bitrepository.protocol.fileexchange.HttpServerConnector;
import org.jaccept.TestEventManager;

public class PillarFileManager {
    private final String pillarID;
    private final Settings mySettings;
    private final ClientProvider clientProvider;
    private final TestEventManager testEventManager;
    private final HttpServerConnector httpServer;
    private int knownNumberOfFilesOnPillar = -1;

    public PillarFileManager(
        String pillarID,
        Settings mySettings,
        ClientProvider clientProvider,
        TestEventManager testEventManager,
        HttpServerConnector httpServer) {
        this.pillarID = pillarID;
        this.mySettings = mySettings;
        this.clientProvider = clientProvider;
        this.httpServer = httpServer;
        this.testEventManager = testEventManager;
    }

    /**
     * Deletes the files one by one on the pillar.
     */
    public void deleteAllFiles() {
        List<ChecksumDataForChecksumSpecTYPE> filesWithChecksums = getChecksums(null, null);
        for (ChecksumDataForChecksumSpecTYPE checksumData: filesWithChecksums) {
            ChecksumDataForFileTYPE checksumDataForFile = new ChecksumDataForFileTYPE();
            checksumDataForFile.setCalculationTimestamp(checksumData.getCalculationTimestamp());
            checksumDataForFile.setChecksumSpec(ChecksumUtils.getDefault(mySettings));
            checksumDataForFile.setChecksumValue(checksumData.getChecksumValue());
            try {
                clientProvider.getDeleteFileClient().deleteFile(
                    checksumData.getFileID(), pillarID, checksumDataForFile, null, null, "");
            } catch (OperationFailedException e) {
                throw new RuntimeException("Failed to delete from pillar " + pillarID, e);
            }
        }
    }

    /**
     * Will ensure that at least <code>desiredNumberOfFiles</code> are present on the pillar. Maintains a counter of
     * how many files where found last time, based on the assumation that the number of files between calls will
     * now decrease. If to few files are initially present, the remaining files are put to the pillar.
     * @param desiredNumberOfFiles
     */
    public void ensureNumberOfFilesOnPillar(int desiredNumberOfFiles, String newFileIDPrefix) {
        addFixtureSetup("Ensuring at least " + desiredNumberOfFiles + " files are present on the pillar " + pillarID);
        if (desiredNumberOfFiles >= knownNumberOfFilesOnPillar) {
            knownNumberOfFilesOnPillar = getFileIDs().size();
        }

        if (desiredNumberOfFiles >= knownNumberOfFilesOnPillar) {
            addFilesToPillar(desiredNumberOfFiles - knownNumberOfFilesOnPillar, newFileIDPrefix);
        }
    }

    public void addFilesToPillar(int numberOfFilesToAdd, String testName) {
        addFixtureSetup("Putting " + numberOfFilesToAdd + " to the pillar");
        String[] newFileIDs = TestFileHelper.createFileIDs(numberOfFilesToAdd, testName);
        for (String newFileID:newFileIDs) {
            try {
                // ToDo: This would be more precise if the client allowed put to a single pillar.
                clientProvider.getPutClient().putFile(httpServer.getURL(TestFileHelper.DEFAULT_FILE_ID), newFileID, 10L,
                    TestFileHelper.getDefaultFileChecksum(), null, null, null);
            } catch (Exception e) {
                throw new RuntimeException("Failed to put file on pillar " + pillarID, e);
            }
        }
    }

    public List<FileIDsDataItem> getFileIDs() {
        ContributorQuery singlePillarQuery = new ContributorQuery(pillarID, null, null, null);
        try {
            List<ContributorEvent> result = clientProvider.getGetFileIDsClient().
                getGetFileIDs(new ContributorQuery[]{singlePillarQuery}, null, null, null);
            FileIDsCompletePillarEvent pillarResult = (FileIDsCompletePillarEvent)result.get(0);
            return pillarResult.getFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fileIDs from pillar " + pillarID, e);
        }
    }

    public List<ChecksumDataForChecksumSpecTYPE> getChecksums(ChecksumSpecTYPE checksumSpec, ContributorQuery query) {
        if (checksumSpec == null) {
            checksumSpec = ChecksumUtils.getDefault(mySettings);
        }

        if (query == null) {
            query = new ContributorQuery(pillarID, null, null, null);
        }

        try {
            List<ContributorEvent> result = clientProvider.getGetChecksumsClient().getChecksums(
                new ContributorQuery[]{query}, null, checksumSpec,  null, null, null);
            ChecksumsCompletePillarEvent pillarResult = (ChecksumsCompletePillarEvent)result.get(0);
            return pillarResult.getChecksums().getChecksumDataItems();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fileIDs from pillar " + pillarID, e);
        }
    }

    private void addFixtureSetup(String setupDescription) {
        testEventManager.addStep(setupDescription, "");
    }
}
