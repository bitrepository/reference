package org.bitrepository.integrityservice;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityservice.collector.DelegatingIntegrityInformationCollector;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test that collecting integrity information has the desired effect.
 */
public class IntegrityInformationCollectorTest extends ExtendedTestCase {

    @Test(groups = "regressiontest")
    public void testCollectorGetFileIDs() throws Exception {
        addDescription("Tests that the collector calls the GetFileClient");
        addStep("Define variables", "No errors");
        String pillarId = "TEST-PILLAR";
        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs("true");
        String auditTrailInformation = "audit trail for this test";
        
        addStep("Setup a GetFileIDsClient for test purpose.", "Should be OK.");
        MockGetFileIDsClient getFileIDs = new MockGetFileIDsClient();
        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(getFileIDs, null);
        
        addStep("Call the getFileIDs on the collector.", "Should go directly to the GetFileIDsClient");
        collector.getFileIDs(Arrays.asList(pillarId), fileIDs, auditTrailInformation, null);
        Assert.assertEquals(getFileIDs.getCallsForGetFileIDs(), 1);
        
        addStep("Call the getFileIDs on the collector four times more.", "The GetFileIDsClient should have been called 5 times.");
        collector.getFileIDs(Arrays.asList(pillarId), fileIDs, auditTrailInformation, null);
        collector.getFileIDs(Arrays.asList(pillarId), fileIDs, auditTrailInformation, null);
        collector.getFileIDs(Arrays.asList(pillarId), fileIDs, auditTrailInformation, null);
        collector.getFileIDs(Arrays.asList(pillarId), fileIDs, auditTrailInformation, null);
        Assert.assertEquals(getFileIDs.getCallsForGetFileIDs(), 5);
    }

    @Test(groups = "regressiontest")
    public void testCollectorGetChecksums() throws Exception {
        addDescription("Tests that the collector calls the GetChecksumsClient");
        addStep("Define variables", "No errors");
        String pillarId = "TEST-PILLAR";
        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs("true");
        ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
        csType.setChecksumType(ChecksumType.MD5);
        String auditTrailInformation = "audit trail for this test";
        
        addStep("Setup a GetChecksumsClient for test purpose.", "Should be OK.");
        MockGetChecksumsClient getChecksumsClient = new MockGetChecksumsClient();
        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(null, getChecksumsClient);
        
        addStep("Call the getChecksumsClient on the collector.", "Should go directly to the GetChecksumsClient");
        collector.getChecksums(Arrays.asList(pillarId), fileIDs, csType, auditTrailInformation, null);
        Assert.assertEquals(getChecksumsClient.getCallsForGetChecksums(), 1);
        
        addStep("Call the getChecksumsClient on the collector four times more.", "The GetChecksumsClient should have been called 5 times.");
        collector.getChecksums(Arrays.asList(pillarId), fileIDs, csType, auditTrailInformation, null);
        collector.getChecksums(Arrays.asList(pillarId), fileIDs, csType, auditTrailInformation, null);
        collector.getChecksums(Arrays.asList(pillarId), fileIDs, csType, auditTrailInformation, null);
        collector.getChecksums(Arrays.asList(pillarId), fileIDs, csType, auditTrailInformation, null);
        Assert.assertEquals(getChecksumsClient.getCallsForGetChecksums(), 5);
    }
    
    private class MockGetFileIDsClient implements GetFileIDsClient {
        int callsForGetFileIDs = 0;
        public int getCallsForGetFileIDs() {
            return callsForGetFileIDs;
        }
        @Override
        public void shutdown() {}
        @Override
        public void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, URL addressForResult,
                EventHandler eventHandler, String auditTrailInformation) throws OperationFailedException {
            callsForGetFileIDs++;
        }
    }

    private class MockGetChecksumsClient implements GetChecksumsClient {
        int callsForGetChecksums = 0;
        public int getCallsForGetChecksums() {
            return callsForGetChecksums;
        }
        @Override
        public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumSpec,
                URL addressForResult, EventHandler eventHandler, String auditTrailInformation)
                throws OperationFailedException {
            callsForGetChecksums++;
        }

        @Override
        public void shutdown() {
            // TODO Auto-generated method stub
            
        }
        
    }

    @Test(groups = "specificationonly")
    void testListFileIDsCollection() throws Exception {
        addDescription("Test that requesting a file list, has the effect that the database is updated with the desired "
                               + "information");
        // TODO old stuff from Kåre. Might be a usecase.
        addStep("Set up the system", "No errors");
        addStep("Request a list of files", "Database is updated with list of files");
    }

    @Test(groups = "specificationonly")
    void testGetChecksumsCollection() throws Exception {
        addDescription("Test that requesting a set if checksums, has the effect that the database is updated with the"
                               + " desired information");
        // TODO old stuff from Kåre. Might be a usecase.
        addStep("Set up the system", "No errors");
        addStep("Request a list of checksums", "Database is updated with list of files");
    }
}
