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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.net.URL;
import java.util.Arrays;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.ContributorQueryUtils;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.modify.putfile.PutFileClient;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

/**
 * Test that collecting integrity information has the desired effect.
 */
public class IntegrityInformationCollectorTest extends ExtendedTestCase {

    public final static String collectionID = "dummy-collection";
    public final static String fileId = "FILE_ID";
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testCollectorGetFileIDs() throws Exception {
        addDescription("Tests that the collector calls the GetFileClient");
        addStep("Define variables", "No errors");
        String pillarId = "TEST-PILLAR";
        ContributorQuery[] contributorQueries = ContributorQueryUtils.createFullContributorQuery(Arrays.asList(pillarId));
        String auditTrailInformation = "audit trail for this test";
        
        addStep("Setup a GetFileIDsClient for test purpose.", "Should be OK.");
        GetFileIDsClient getFileIDsClient = mock(GetFileIDsClient.class);
        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(getFileIDsClient, null, null, null);
        EventHandler eventHandler = mock(EventHandler.class);
        
        addStep("Call the getFileIDs on the collector.", "Should go directly to the GetFileIDsClient");
        collector.getFileIDs(collectionID, Arrays.asList(pillarId), auditTrailInformation, contributorQueries, eventHandler);
        verify(getFileIDsClient).getFileIDs(eq(collectionID), any(), anyString(), any(URL.class), any(EventHandler.class));
        
        addStep("Call the getFileIDs on the collector four times more.", 
                "The GetFileIDsClient should have been called 5 times.");
        collector.getFileIDs(collectionID, Arrays.asList(pillarId), auditTrailInformation, contributorQueries, eventHandler);
        collector.getFileIDs(collectionID, Arrays.asList(pillarId), auditTrailInformation, contributorQueries, eventHandler);
        collector.getFileIDs(collectionID, Arrays.asList(pillarId), auditTrailInformation, contributorQueries, eventHandler);
        collector.getFileIDs(collectionID, Arrays.asList(pillarId), auditTrailInformation, contributorQueries, eventHandler);
        verify(getFileIDsClient, times(5)).getFileIDs(eq(collectionID), any(), anyString(), any(URL.class), any(EventHandler.class));
        
        verifyNoMoreInteractions(getFileIDsClient);
        verifyNoMoreInteractions(eventHandler);
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
        GetChecksumsClient getChecksumsClient = mock(GetChecksumsClient.class);
        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(
                null, getChecksumsClient, null, null);
        EventHandler eventHandler = mock(EventHandler.class);
        
        addStep("Call the getChecksumsClient on the collector.", "Should go directly to the GetChecksumsClient");
        collector.getChecksums(collectionID, Arrays.asList(pillarId), csType, auditTrailInformation, contributorQueries, eventHandler);
        verify(getChecksumsClient).getChecksums(eq(collectionID), any(), anyString(), any(ChecksumSpecTYPE.class), any(URL.class), any(EventHandler.class), anyString());
        
        addStep("Call the getChecksumsClient on the collector four times more.", 
                "The GetChecksumsClient should have been called 5 times.");
        collector.getChecksums(collectionID, Arrays.asList(pillarId), csType, auditTrailInformation, contributorQueries, eventHandler);
        collector.getChecksums(collectionID, Arrays.asList(pillarId), csType, auditTrailInformation, contributorQueries, eventHandler);
        collector.getChecksums(collectionID, Arrays.asList(pillarId), csType, auditTrailInformation, contributorQueries, eventHandler);
        collector.getChecksums(collectionID, Arrays.asList(pillarId), csType, auditTrailInformation, contributorQueries, eventHandler);
        verify(getChecksumsClient, times(5)).getChecksums(eq(collectionID), any(), anyString(), any(ChecksumSpecTYPE.class), any(URL.class), 
                any(EventHandler.class), anyString());
        
        verifyNoMoreInteractions(getChecksumsClient);
        verifyNoMoreInteractions(eventHandler);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testCollectorGetFile() throws Exception {
        addDescription("Tests that the collector calls the GetFileClient");
        addStep("Define variables", "No errors");
        String auditTrailInformation = "audit trail for this test";
        URL uploadUrl = new URL("http://localhost:80/dav/test.txt");
        
        addStep("Setup a GetFileClient for test purpose.", "Should be OK.");
        GetFileClient getFileClient = mock(GetFileClient.class);
        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(
                null, null, getFileClient, null);
        EventHandler eventHandler = mock(EventHandler.class);

        addStep("Call the GetFileClient on the collector.", "Should go directly to the GetFileClient");
        collector.getFile(collectionID, fileId, uploadUrl, eventHandler, auditTrailInformation);
        verify(getFileClient).getFileFromFastestPillar(eq(collectionID), eq(fileId), any(), eq(uploadUrl), any(EventHandler.class), eq(auditTrailInformation));
        
        addStep("Call the GetFileClient on the collector four times more.", 
                "The GetFileClient should have been called 5 times.");
        collector.getFile(collectionID, fileId, uploadUrl, eventHandler, auditTrailInformation);
        collector.getFile(collectionID, fileId, uploadUrl, eventHandler, auditTrailInformation);
        collector.getFile(collectionID, fileId, uploadUrl, eventHandler, auditTrailInformation);
        collector.getFile(collectionID, fileId, uploadUrl, eventHandler, auditTrailInformation);
        verify(getFileClient, times(5)).getFileFromFastestPillar(eq(collectionID), eq(fileId), any(), eq(uploadUrl), any(EventHandler.class), eq(auditTrailInformation));
        verifyNoMoreInteractions(getFileClient);
        verifyNoMoreInteractions(eventHandler);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testCollectorPutFile() throws Exception {
        addDescription("Tests that the collector calls the PutFileClient");
        addStep("Define variables", "No errors");
        String auditTrailInformation = "audit trail for this test";
        URL uploadUrl = new URL("http://localhost:80/dav/test.txt");
        ChecksumDataForFileTYPE csForValidation = new ChecksumDataForFileTYPE();
        
        addStep("Setup a PutFileClient for test purpose.", "Should be OK.");
        PutFileClient putFileClient = mock(PutFileClient.class);
        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(
                null, null, null, putFileClient);
        EventHandler eventHandler = mock(EventHandler.class);

        addStep("Call the PutFileClient on the collector.", "Should go directly to the PutFileClient");
        collector.putFile(collectionID, fileId, uploadUrl, csForValidation, eventHandler, auditTrailInformation);
        verify(putFileClient).putFile(eq(collectionID), eq(uploadUrl), eq(fileId), anyLong(), any(ChecksumDataForFileTYPE.class), 
                any(ChecksumSpecTYPE.class), any(EventHandler.class), eq(auditTrailInformation));
        
        addStep("Call the PutFileClient on the collector four times more.", 
                "The PutFileClient should have been called 5 times.");
        collector.putFile(collectionID, fileId, uploadUrl, csForValidation, eventHandler, auditTrailInformation);
        collector.putFile(collectionID, fileId, uploadUrl, csForValidation, eventHandler, auditTrailInformation);
        collector.putFile(collectionID, fileId, uploadUrl, csForValidation, eventHandler, auditTrailInformation);
        collector.putFile(collectionID, fileId, uploadUrl, csForValidation, eventHandler, auditTrailInformation);
        verify(putFileClient, times(5)).putFile(eq(collectionID), eq(uploadUrl), eq(fileId), anyLong(), any(ChecksumDataForFileTYPE.class), 
                any(ChecksumSpecTYPE.class), any(EventHandler.class), eq(auditTrailInformation));
        verifyNoMoreInteractions(putFileClient);
        verifyNoMoreInteractions(eventHandler);
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

        addStep("Setup a failing GetChecksumClient for test purpose.", "Should be OK.");
        GetChecksumsClient getChecksumsClient = mock(GetChecksumsClient.class);
        doThrow(new RuntimeException()).when(getChecksumsClient).getChecksums(anyString(), any(), anyString(), any(ChecksumSpecTYPE.class), any(URL.class), any(EventHandler.class), anyString());

        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(null, getChecksumsClient, null, null);
        
        addStep("Verify that the collector does not fail, just because the GetChecksumClient does so", 
                "Should not throw an unexpected exception");
        collector.getChecksums(collectionID, Arrays.asList(pillarId), csType, auditTrailInformation, contributorQueries, null);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testCollectorHandleGetFileIDsClientFailures() throws Exception {
        addDescription("Test that the IntegrityInformationCollector works as a fault-barrier.");
        addStep("Setup variables for the test", "Should be OK");
        String pillarId = "TEST-PILLAR";
        ContributorQuery[] contributorQueries = ContributorQueryUtils.createFullContributorQuery(
                Arrays.asList(pillarId));
        String auditTrailInformation = "audit trail for this test";

        addStep("Setup a DyingGetFileIDsClient for test purpose.", "Should be OK.");
        GetFileIDsClient getFileIDsClient = mock(GetFileIDsClient.class);
        doThrow(new RuntimeException()).when(getFileIDsClient).getFileIDs(anyString(), any(), anyString(), any(URL.class), any(EventHandler.class));
        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(getFileIDsClient, null, null, null);
        
        addStep("Verify that the collector does not fail, just because the GetChecksumClient does so", 
                "Should not throw an unexpected exception");
        collector.getFileIDs(collectionID, Arrays.asList(pillarId), auditTrailInformation, contributorQueries, null);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testCollectorHandleGetFileClientFailures() throws Exception {
        addDescription("Test that the IntegrityInformationCollector works as a fault-barrier.");
        addStep("Define variables", "No errors");
        String auditTrailInformation = "audit trail for this test";
        URL uploadUrl = new URL("http://localhost:80/dav/test.txt");
        
        addStep("Setup a GetFileClient for test purpose, and ensure that it throws an error when called.", "Should be OK.");
        GetFileClient getFileClient = mock(GetFileClient.class);
        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(
                null, null, getFileClient, null);
        EventHandler eventHandler = mock(EventHandler.class);
        doThrow(new RuntimeException()).when(getFileClient).getFileFromFastestPillar(anyString(), anyString(), any(FilePart.class), 
                any(URL.class), any(EventHandler.class), anyString());

        addStep("Verify that the collector does not fail, just because the GetFileClient does so", 
                "Should not throw an unexpected exception");
        collector.getFile(collectionID, fileId, uploadUrl, eventHandler, auditTrailInformation);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testCollectorHandlePutFileClientFailures() throws Exception {
        addDescription("Test that the IntegrityInformationCollector works as a fault-barrier.");
        addStep("Define variables", "No errors");
        String auditTrailInformation = "audit trail for this test";
        URL uploadUrl = new URL("http://localhost:80/dav/test.txt");
        ChecksumDataForFileTYPE csForValidation = new ChecksumDataForFileTYPE();
        
        addStep("Setup a PutFileClient for test purpose, and ensure that it throws an error when called.", "Should be OK.");
        PutFileClient putFileClient = mock(PutFileClient.class);
        IntegrityInformationCollector collector = new DelegatingIntegrityInformationCollector(
                null, null, null, putFileClient);
        EventHandler eventHandler = mock(EventHandler.class);
        doThrow(new RuntimeException()).when(putFileClient).putFile(anyString(), any(URL.class), anyString(), anyLong(), any(ChecksumDataForFileTYPE.class), 
                any(ChecksumSpecTYPE.class), any(EventHandler.class), anyString());

        addStep("Verify that the collector does not fail, just because the PutFileClient does so", 
                "Should not throw an unexpected exception");
        collector.putFile(collectionID, fileId, uploadUrl, csForValidation, eventHandler, auditTrailInformation);
    }
}
