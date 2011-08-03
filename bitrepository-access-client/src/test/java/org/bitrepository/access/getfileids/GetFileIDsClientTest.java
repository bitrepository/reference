/*
 * #%L
 * Bitmagasin integrationstest
 * *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access.getfileids;

import javax.xml.bind.JAXBException;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.clienttest.DefaultFixtureClientTest;
import org.bitrepository.protocol.fileexchange.TestFileStore;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for the 'GetFileIDsClient'.
 * @author kfc
 */
public class GetFileIDsClientTest extends DefaultFixtureClientTest {

    private TestGetFileIDsMessageFactory testMessageFactory;
    private TestFileStore pillar1FileStore;
    private TestFileStore pillar2FileStore;

    /**
     * Set up the test scenario before running the tests in this class.
     * @throws javax.xml.bind.JAXBException
     */
    @BeforeMethod(alwaysRun = true)
    public void setUp() throws JAXBException {
        // TODO getFileIDsFromFastestPillar settings
        if (useMockupPillar()) {
            testMessageFactory = new TestGetFileIDsMessageFactory(settings.getBitRepositoryCollectionID());
            pillar1FileStore = new TestFileStore("Pillar1");
            pillar2FileStore = new TestFileStore("Pillar2");
        }
        httpServer.clearFiles();
    }

    /**
     * Test the get file IDs functionality of the GetFileIDsClient.
     * Corresponds to user story described on https://sbforge.org/display/BITMAG/Get+File+IDs+User+Stories
     * @throws Exception
     */
    @Test(groups = {"specificationonly"})
    public void getFileIDsFromFastestPillarTest() throws Exception {
        addDescription("Test the get file IDs functionality of the GetFileIDsClient. " +
                "We should get a result from the fastest pillar.");

        addStep("Testing getFileIDsFromFastestPillar(bitRepositoryCollectionID, fileIDs) method.",
                "Logging of identifyPillarForGetFileIDsRequest message and response messages; logging of " +
                        "getFileIDsRequest, getFileIDsProgressResponse, getFileIDsFinalResponse. " +
                        "Assertion result received.");

        GetFileIDsClient getFileIDsClient = new GetFileIDsClientTestWrapper(
                AccessComponentFactory.getInstance().createGetFileIDsClient(), testEventManager);

        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs("ALL");
        ResultingFileIDs resultingFileIDs =
                getFileIDsClient.getFileIDsFromFastestPillar(settings.getBitRepositoryCollectionID(), fileIDs);
        Assert.assertNotNull(resultingFileIDs, "resultingFileIDs should not be null");

        addStep("Testing getFileIDsFromFastestPillar(bitRepositoryCollectionID, fileIDs, uploadUrl) method.",
                "Logging of identifyPillarForGetFileIDsRequest message and response messages; logging of " +
                        "getFileIDsRequest, getFileIDsProgressResponse, getFileIDsFinalResponse. " +
                        "Assertion result uploaded.");

        addStep("Testing getFileIDsFromFastestPillar(bitRepositoryCollectionID, fileIDs, uploadUrl, eventHandler) method.",
                "Logging of identifyPillarForGetFileIDsRequest message and response messages; logging of " +
                        "getFileIDsRequest, getFileIDsProgressResponse, getFileIDsFinalResponse. " +
                        "Assertion result uploaded.");

    }

    /**
     * Test the get file IDs from specified pillar functionality of the GetFileIDsClient.
     * @throws Exception
     */
    @Test(groups = {"specificationonly"})
    public void getFileIDsFromSpecificPillarTest() throws Exception {
        addDescription("Test the get file IDs from specified pillar functionality of the GetFileIDsClient. " +
                "We should get a result from the specified pillar.");

        addStep("Testing getFileIDsFromSpecificPillar(pillarID, bitRepositoryCollectionID, fileIDs) method.",
                "Logging of identifyPillarForGetFileIDsRequest message and response messages; logging of " +
                        "getFileIDsRequest, getFileIDsProgressResponse, getFileIDsFinalResponse from specified " +
                        "pillar. Assertion result received.");

        addStep("Testing getFileIDsFromSpecificPillar(pillarID, bitRepositoryCollectionID, fileIDs, uploadUrl) method.",
                "Logging of identifyPillarForGetFileIDsRequest message and response messages; logging of " +
                        "getFileIDsRequest, getFileIDsProgressResponse, getFileIDsFinalResponse from specified " +
                        "pillar. Assertion result uploaded.");

        addStep("Testing getFileIDsFromSpecificPillar(pillarID, bitRepositoryCollectionID, fileIDs, uploadUrl, eventHandler) " +
                "method.", "Logging of identifyPillarForGetFileIDsRequest message and response messages; logging of " +
                        "getFileIDsRequest, getFileIDsProgressResponse, getFileIDsFinalResponse from specified " +
                        "pillar. Assertion result uploaded.");

    }


    /**
     * Test the results from the GetFileIDsClient.
     * This test also tests the responses of the pillars.
     * To test only the results of the Client, we need to know the
     * exact responses of the pillars.
     * @throws Exception
     */
    @Test(groups = {"specificationonly"})
    public void getFileIDsResultTest() throws Exception {
        addDescription("Tests the result list of FileIds from the client and thus the involved pillars.");

        addStep("Test known fileIDs part of FileIDs list returned from getFileIDsFromFastestPillar method.",
                "The returned list of FileIDs should contain the known IDs.");

        addStep("Test removed fileID NOT part of FileIDs list returned from getFileIDsFromFastestPillar method (remove one of the " +
                "files with known IDs from the Bit Repository under given BRC ID).",
                "The returned list of FileIDs should NOT contain the ID of the file just removed.");
    }

}
