/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityservice.workflow;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.workflow.Workflow;
import org.bitrepository.settings.referencesettings.ProtocolType;
import org.jaccept.structure.ExtendedTestCase;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@SuppressWarnings("rawtypes")
public class RepairMissingFilesWorkflowTest extends ExtendedTestCase {
    
    private static final String PILLAR_1 = "pillar1";
    private static final String PILLAR_2 = "pillar2";
    
    private static final String DEFAULT_CHECKSUM = "0123456789";
    private static final String TEST_FILE_1 = "test-file-1";
    private String TEST_COLLECTION;

    protected Settings settings;
    protected IntegrityInformationCollector collector;
    protected IntegrityAlerter alerter;
    protected IntegrityModel model;
    protected AuditTrailManager auditManager;
    
    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityWorkflowTest");

        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_1);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_2);
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(0);
        
        settings.getReferenceSettings().getFileExchangeSettings().setProtocolType(ProtocolType.HTTP);
        settings.getReferenceSettings().getFileExchangeSettings().setPath("dav");
        settings.getReferenceSettings().getFileExchangeSettings().setPort(BigInteger.valueOf(80));
        settings.getReferenceSettings().getFileExchangeSettings().setServerName("localhost");        
        
        TEST_COLLECTION = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        SettingsUtils.initialize(settings);
        
        collector = mock(IntegrityInformationCollector.class);
        alerter = mock(IntegrityAlerter.class);
        model = mock(IntegrityModel.class);
        auditManager = mock(AuditTrailManager.class);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testNoMissingFiles() throws Exception {
        addDescription("Test that the workflow does nothing, when it has no missing files.");
        addStep("Prepare for calls to mocks", "");
        when(model.getMissingFilesAtPillarByIterator(anyString(), anyInt(), anyInt(), eq(TEST_COLLECTION))).thenReturn(createMockIterator(new String[0]));

        addStep("Run workflow for repairing missing files.", "Should not try to repair anything.");

        Workflow workflow = new RepairMissingFilesWorkflow();
        IntegrityWorkflowContext context = new IntegrityWorkflowContext(settings, collector, model, alerter, auditManager);
        workflow.initialise(context, TEST_COLLECTION);

        workflow.start();
        
        verifyNoMoreInteractions(collector);
        verifyNoMoreInteractions(alerter);
        verifyNoMoreInteractions(auditManager);
        
        verify(model, times(2)).getMissingFilesAtPillarByIterator(anyString(), anyInt(), anyInt(), eq(TEST_COLLECTION));
        verifyNoMoreInteractions(model);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testSuccesRepair() throws Exception {
        addDescription("Test that the workflow makes calls to the collector, when a file is missing");
        addStep("Prepare for calls to mocks to handle a repair", "");
        when(model.getMissingFilesAtPillarByIterator(anyString(), anyInt(), anyInt(), eq(TEST_COLLECTION))).thenReturn(createMockIterator(TEST_FILE_1));

        when(model.getFileInfos(eq(TEST_FILE_1), eq(TEST_COLLECTION))).thenReturn(createMockFileInfo(TEST_FILE_1, DEFAULT_CHECKSUM, PILLAR_1));

        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[3];
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getFile(
                anyString(), anyString(), any(URL.class), any(EventHandler.class), anyString());
        
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[4];
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).putFile(
                anyString(), anyString(), any(URL.class), any(ChecksumDataForFileTYPE.class), any(EventHandler.class), anyString());

        addStep("Run workflow for repairing missing files.", "Should find one missing file and try to repair it by using put-file and get-file operations on the collector.");

        Workflow workflow = new RepairMissingFilesWorkflow();
        IntegrityWorkflowContext context = new IntegrityWorkflowContext(settings, collector, model, alerter, auditManager);
        workflow.initialise(context, TEST_COLLECTION);

        workflow.start();

        verifyNoMoreInteractions(alerter);
        verifyNoMoreInteractions(auditManager);

        verify(collector).getFile(eq(TEST_COLLECTION), eq(TEST_FILE_1), any(URL.class), any(EventHandler.class), anyString());
        verify(collector).putFile(eq(TEST_COLLECTION), eq(TEST_FILE_1), any(URL.class), any(), any(EventHandler.class), anyString());
        verifyNoMoreInteractions(collector);
        
        verify(model, times(2)).getMissingFilesAtPillarByIterator(anyString(), anyInt(), anyInt(), eq(TEST_COLLECTION));
        verify(model).getFileInfos(eq(TEST_FILE_1), eq(TEST_COLLECTION));
        verifyNoMoreInteractions(model);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testFailedGetFile() throws Exception {
        addDescription("Test that the workflow does not try to put a file, if it fails to get it.");
        addStep("Prepare for calls to mocks to fail when performing get-file", "");
        when(model.getMissingFilesAtPillarByIterator(anyString(), anyInt(), anyInt(), eq(TEST_COLLECTION))).thenReturn(createMockIterator(TEST_FILE_1));

        when(model.getFileInfos(eq(TEST_FILE_1), eq(TEST_COLLECTION))).thenReturn(createMockFileInfo(TEST_FILE_1, DEFAULT_CHECKSUM, PILLAR_1));

        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[3];
                eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "failure", null));
                return null;
            }
        }).when(collector).getFile(
                anyString(), anyString(), any(URL.class), any(EventHandler.class), anyString());
        
        addStep("Run missing checksum step.", "Should fail during get-file, thus not performing put-file. Also workflow should send an alarm.");

        Workflow workflow = new RepairMissingFilesWorkflow();
        IntegrityWorkflowContext context = new IntegrityWorkflowContext(settings, collector, model, alerter, auditManager);
        workflow.initialise(context, TEST_COLLECTION);

        workflow.start();

        verifyNoMoreInteractions(auditManager);

        verify(alerter).operationFailed(anyString(), eq(TEST_COLLECTION));
        verifyNoMoreInteractions(alerter);

        verify(collector).getFile(eq(TEST_COLLECTION), eq(TEST_FILE_1), any(URL.class), any(EventHandler.class), anyString());
        verifyNoMoreInteractions(collector);
        
        verify(model, times(2)).getMissingFilesAtPillarByIterator(anyString(), anyInt(), anyInt(), eq(TEST_COLLECTION));
        verify(model).getFileInfos(eq(TEST_FILE_1), eq(TEST_COLLECTION));
        verifyNoMoreInteractions(model);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testFailedPutFile() throws Exception {
        addDescription("Test that the workflow makes calls to the collector for get and put file, even when put file fails.");
        addStep("Prepare for calls to mocks", "");
        when(model.getMissingFilesAtPillarByIterator(anyString(), anyInt(), anyInt(), eq(TEST_COLLECTION))).thenReturn(createMockIterator(TEST_FILE_1));

        when(model.getFileInfos(eq(TEST_FILE_1), eq(TEST_COLLECTION))).thenReturn(createMockFileInfo(TEST_FILE_1, DEFAULT_CHECKSUM, PILLAR_1));


        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[3];
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getFile(
                anyString(), anyString(), any(URL.class), any(EventHandler.class), anyString());
        
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[4];
                eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "failure", null));
                return null;
            }
        }).when(collector).putFile(
                anyString(), anyString(), any(URL.class), any(ChecksumDataForFileTYPE.class), any(EventHandler.class), anyString());

        addStep("Run workflow to repair missing files.", "Should both get-file and put-file, but fail at put-file and then send an alarm.");

        Workflow workflow = new RepairMissingFilesWorkflow();
        IntegrityWorkflowContext context = new IntegrityWorkflowContext(settings, collector, model, alerter, auditManager);
        workflow.initialise(context, TEST_COLLECTION);

        workflow.start();

        verifyNoMoreInteractions(auditManager);

        verify(alerter).operationFailed(anyString(), eq(TEST_COLLECTION));
        verifyNoMoreInteractions(alerter);

        verify(collector).getFile(eq(TEST_COLLECTION), eq(TEST_FILE_1), any(URL.class), any(EventHandler.class), anyString());
        verify(collector).putFile(eq(TEST_COLLECTION), eq(TEST_FILE_1), any(URL.class), any(), any(EventHandler.class), anyString());
        verifyNoMoreInteractions(collector);
        
        verify(model, times(2)).getMissingFilesAtPillarByIterator(anyString(), anyInt(), anyInt(), eq(TEST_COLLECTION));
        verify(model).getFileInfos(eq(TEST_FILE_1), eq(TEST_COLLECTION));
        verifyNoMoreInteractions(model);
    }
    
    private IntegrityIssueIterator createMockIterator(String ...strings) {
        return new IntegrityIssueIterator(null) {
            Iterator<String> results = Arrays.asList(strings).iterator();
            @Override
            public void close() {
                // TODO Auto-generated method stub
                super.close();
            }
            
            @Override
            public String getNextIntegrityIssue() {
                if(results.hasNext()) {
                    return results.next();
                }
                return null;
            }
        };
    }
    
    private List<FileInfo> createMockFileInfo(String fileId, String checksum, String ... pillars) {
        List<FileInfo> res = new ArrayList<FileInfo>();
        for(String pillar : pillars) {
            res.add(new FileInfo(fileId, null, checksum, 0L, null, pillar));
        }
        return res;
    }
}
