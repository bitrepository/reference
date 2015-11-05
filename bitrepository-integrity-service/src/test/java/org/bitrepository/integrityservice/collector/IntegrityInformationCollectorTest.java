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
package org.bitrepository.integrityservice.collector;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.ContributorQueryUtils;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.client.eventhandler.EventHandler;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.Arrays;

/**
 * Test that collecting integrity information has the desired effect.
 */
public class IntegrityInformationCollectorTest extends ExtendedTestCase {

    public final static String collectionID = "dummy-collection";
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testCollectorGetFileIDs() throws Exception {
        addDescription("Tests that the collector calls the GetFileClient");
        addStep("Define variables", "No errors");
        String pillarId = "TEST-PILLAR";
        ContributorQuery[] contributorQueries = ContributorQueryUtils.createFullContributorQuery(Arrays.asList(pillarId));
        String auditTrailInformation = "audit trail for this test";
        
        addStep("Setup a GetFileIDsClient for test purpose.", "Should be OK.");
        MockGetFileIDsClient getFileIDs = new MockGetFileIDsClient();
        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(getFileIDs, null, null, null);
        
        addStep("Call the getFileIDs on the collector.", "Should go directly to the GetFileIDsClient");
        collector.getFileIDs(collectionID, Arrays.asList(pillarId), auditTrailInformation, contributorQueries, null);
        Assert.assertEquals(getFileIDs.getCallsForGetFileIDs(), 1);
        
        addStep("Call the getFileIDs on the collector four times more.", 
                "The GetFileIDsClient should have been called 5 times.");
        collector.getFileIDs(collectionID, Arrays.asList(pillarId), auditTrailInformation, contributorQueries, null);
        collector.getFileIDs(collectionID, Arrays.asList(pillarId), auditTrailInformation, contributorQueries, null);
        collector.getFileIDs(collectionID, Arrays.asList(pillarId), auditTrailInformation, contributorQueries, null);
        collector.getFileIDs(collectionID, Arrays.asList(pillarId), auditTrailInformation, contributorQueries, null);
        Assert.assertEquals(getFileIDs.getCallsForGetFileIDs(), 5);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testCollectorGetChecksums() throws Exception {
        addDescription("Tests that the collector calls the GetChecksumsClient");
        addStep("Define variables", "No errors");
        String pillarId = "TEST-PILLAR";
        ContributorQuery[] contributorQueries = ContributorQueryUtils.createFullContributorQuery(
                Arrays.asList(pillarId));
        ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
        csType.setChecksumType(ChecksumType.MD5);
        String auditTrailInformation = "audit trail for this test";
        
        addStep("Setup a GetChecksumsClient for test purpose.", "Should be OK.");
        MockGetChecksumsClient getChecksumsClient = new MockGetChecksumsClient();
        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(
                null, getChecksumsClient, null, null);
        
        addStep("Call the getChecksumsClient on the collector.", "Should go directly to the GetChecksumsClient");
        collector.getChecksums(collectionID, Arrays.asList(pillarId), csType, auditTrailInformation, contributorQueries, null);
        Assert.assertEquals(getChecksumsClient.getCallsForGetChecksums(), 1);
        
        addStep("Call the getChecksumsClient on the collector four times more.", 
                "The GetChecksumsClient should have been called 5 times.");
        collector.getChecksums(collectionID, Arrays.asList(pillarId), csType, auditTrailInformation, contributorQueries, null);
        collector.getChecksums(collectionID, Arrays.asList(pillarId), csType, auditTrailInformation, contributorQueries, null);
        collector.getChecksums(collectionID, Arrays.asList(pillarId), csType, auditTrailInformation, contributorQueries, null);
        collector.getChecksums(collectionID, Arrays.asList(pillarId), csType, auditTrailInformation, contributorQueries, null);
        Assert.assertEquals(getChecksumsClient.getCallsForGetChecksums(), 5);
    }
    
    private class MockGetFileIDsClient implements GetFileIDsClient {
        int callsForGetFileIDs = 0;
        public int getCallsForGetFileIDs() {
            return callsForGetFileIDs;
        }

        @Override
        public void getFileIDs(String collectionID, ContributorQuery[] contributorQueries, String fileID,
                               URL addressForResult, EventHandler eventHandler) {
            callsForGetFileIDs++;
        }
    }

    private class MockGetChecksumsClient implements GetChecksumsClient {
        int callsForGetChecksums = 0;
        public int getCallsForGetChecksums() {
            return callsForGetChecksums;
        }

        @Override
        public void getChecksums(String collectionID, ContributorQuery[] contributorQueries, String fileID,
                                 ChecksumSpecTYPE checksumSpec,
                               URL addressForResult, EventHandler eventHandler, String auditTrailInformation) {
            callsForGetChecksums++;
        }
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testCollectorHandleChecksumClientFailures() throws Exception {
        addDescription("Test that the IntegrityInformationCollector works as a fault-barrier.");
        addStep("Setup variables for the test", "Should be OK");
        String pillarId = "TEST-PILLAR";
        ContributorQuery[] contributorQueries = ContributorQueryUtils.createFullContributorQuery(
                Arrays.asList(pillarId));
        ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
        csType.setChecksumType(ChecksumType.MD5);
        String auditTrailInformation = "audit trail for this test";

        addStep("Setup a FailingGetChecksumClient for test purpose.", "Should be OK.");
        DyingGetChecksumClient getDyingChecksumsClient = new DyingGetChecksumClient();
        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(
                null, getDyingChecksumsClient, null, null);
        
        addStep("Verify that the collector does not fail, just because the GetChecksumClient does so", 
                "Should not throw an unexpected exception");
        collector.getChecksums(collectionID, Arrays.asList(pillarId), csType, auditTrailInformation, contributorQueries, null);
    }

    private class DyingGetChecksumClient implements GetChecksumsClient {
        @Override
        public void getChecksums(String collectionID, ContributorQuery[] contributorQueries, String fileID,
                                 ChecksumSpecTYPE checksumSpec, URL addressForResult, EventHandler eventHandler, String auditTrailInformation) {
            throw new RuntimeException("My purpose is to die!");
        }
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testCollectorHandleGetFileIDsClientFailures() throws Exception {
        addDescription("Test that the IntegrityInformationCollector works as a fault-barrier.");
        addStep("Setup variables for the test", "Should be OK");
        String pillarId = "TEST-PILLAR";
        ContributorQuery[] contributorQueries = ContributorQueryUtils.createFullContributorQuery(
                Arrays.asList(pillarId));
        String auditTrailInformation = "audit trail for this test";

        addStep("Setup a FailingGetChecksumClient for test purpose.", "Should be OK.");
        DyingGetFileIDsClient getDyingFileIDsClient = new DyingGetFileIDsClient();
        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(
                getDyingFileIDsClient, null, null, null);
        
        addStep("Verify that the collector does not fail, just because the GetChecksumClient does so", 
                "Should not throw an unexpected exception");
        collector.getFileIDs(collectionID, Arrays.asList(pillarId), auditTrailInformation, contributorQueries, null);
    }

    private class DyingGetFileIDsClient implements GetFileIDsClient {
        @Override
        public void getFileIDs(String collectionID, ContributorQuery[] contributorQueries, String fileID,
                               URL addressForResult,
                               EventHandler eventHandler) {
            throw new RuntimeException("My purpose is to die!");
        }
    }
}
