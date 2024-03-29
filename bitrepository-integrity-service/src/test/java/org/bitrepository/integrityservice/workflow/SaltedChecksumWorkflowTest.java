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

import org.apache.commons.codec.DecoderException;
import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.ContributorFailedEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.workflow.Workflow;
import org.jaccept.structure.ExtendedTestCase;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class SaltedChecksumWorkflowTest extends ExtendedTestCase {

    private static final String PILLAR_1 = "pillar1";
    private static final String PILLAR_2 = "pillar2";
    private static final String PILLAR_3 = "pillar3";

    private static final String TEST_FILE_1 = "test-file-1";
    private String TEST_COLLECTION;

    protected Settings settings;
    protected IntegrityInformationCollector collector;
    protected IntegrityAlerter alerter;
    protected IntegrityModel model;
    protected AuditTrailManager auditManager;

    @BeforeMethod(alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityWorkflowTest");

        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_1);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_2);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_3);

        TEST_COLLECTION = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        SettingsUtils.initialize(settings);

        collector = mock(IntegrityInformationCollector.class);
        alerter = mock(IntegrityAlerter.class);
        model = mock(IntegrityModel.class);
        auditManager = mock(AuditTrailManager.class);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testNoFilesInCollection() {
        addDescription("Test that the workflow does nothing, when it has no files in the collection.");
        addStep("Prepare for calls to mocks", "");
        when(model.getNumberOfFilesInCollection(anyString())).thenReturn(Long.valueOf(0));

        addStep("Run workflow for checking salted checksum.", "Should send alarm about failure");

        Workflow workflow = new SaltedChecksumWorkflow();
        IntegrityWorkflowContext context = new IntegrityWorkflowContext(settings, collector, model, alerter, auditManager);
        workflow.initialise(context, TEST_COLLECTION);
        workflow.start();

        verify(alerter).integrityFailed(anyString(), eq(TEST_COLLECTION));
        verifyNoMoreInteractions(alerter);

        verifyNoInteractions(collector);
        verifyNoInteractions(auditManager);

        verify(model).getNumberOfFilesInCollection(eq(TEST_COLLECTION));
        verifyNoMoreInteractions(model);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testSuccess() {
        addDescription("Test that the workflow works when both pillars deliver the same checksum.");
        addStep("Prepare for calls to mocks", "");
        when(model.getNumberOfFilesInCollection(anyString())).thenReturn(Long.valueOf(1));
        when(model.getFileIDAtPosition(eq(TEST_COLLECTION), eq(0L))).thenReturn(TEST_FILE_1);

        doAnswer((Answer<Void>) invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            ResultingChecksums res = createResultingChecksums((String) invocation.getArguments()[3], "abcdef");
            eventHandler.handleEvent(
                    new ChecksumsCompletePillarEvent(PILLAR_1, TEST_COLLECTION, res, (ChecksumSpecTYPE) invocation.getArguments()[2],
                            false));
            eventHandler.handleEvent(
                    new ChecksumsCompletePillarEvent(PILLAR_2, TEST_COLLECTION, res, (ChecksumSpecTYPE) invocation.getArguments()[2],
                            false));
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
            return null;
        }).when(collector).getChecksums(anyString(), any(), any(), anyString(), anyString(), any(), any(EventHandler.class));

        addStep("Run workflow for checking salted checksum.", "Should send alarm about failure");

        Workflow workflow = new SaltedChecksumWorkflow();
        IntegrityWorkflowContext context = new IntegrityWorkflowContext(settings, collector, model, alerter, auditManager);
        workflow.initialise(context, TEST_COLLECTION);
        workflow.start();

        verifyNoInteractions(alerter);

        verify(collector).getChecksums(eq(TEST_COLLECTION), any(), any(), anyString(), anyString(), any(), any(EventHandler.class));
        verifyNoMoreInteractions(collector);

        verify(model).getNumberOfFilesInCollection(eq(TEST_COLLECTION));
        verify(model).getFileIDAtPosition(eq(TEST_COLLECTION), eq(0L));
        verifyNoMoreInteractions(model);

        verify(auditManager).addAuditEvent(eq(TEST_COLLECTION), anyString(), anyString(), anyString(), anyString(), any(), any(), any());
        verifyNoMoreInteractions(auditManager);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testOneComponentFailureAndTwoOtherAgreeOnChecksum() {
        addDescription("Test that the workflow works when both pillars deliver the same checksum.");
        addStep("Prepare for calls to mocks", "");
        when(model.getNumberOfFilesInCollection(anyString())).thenReturn(Long.valueOf(1));
        when(model.getFileIDAtPosition(eq(TEST_COLLECTION), eq(0L))).thenReturn(TEST_FILE_1);

        doAnswer((Answer<Void>) invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            ResultingChecksums res = createResultingChecksums((String) invocation.getArguments()[3], "abcdef");
            ContributorEvent e1 = new ChecksumsCompletePillarEvent(PILLAR_1, TEST_COLLECTION, res,
                    (ChecksumSpecTYPE) invocation.getArguments()[2], false);
            ContributorEvent e2 = new ChecksumsCompletePillarEvent(PILLAR_2, TEST_COLLECTION, res,
                    (ChecksumSpecTYPE) invocation.getArguments()[2], false);
            ContributorEvent e3 = new ContributorFailedEvent(PILLAR_3, TEST_COLLECTION, ResponseCode.FAILURE);
            eventHandler.handleEvent(e1);
            eventHandler.handleEvent(e2);
            eventHandler.handleEvent(e3);
            eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "COMPONENT FAILED", Arrays.asList(e1, e2, e3)));
            return null;
        }).when(collector).getChecksums(anyString(), any(), any(), anyString(), anyString(), any(), any(EventHandler.class));

        addStep("Run workflow for checking salted checksum.", "Should send alarm about failure");

        Workflow workflow = new SaltedChecksumWorkflow();
        IntegrityWorkflowContext context = new IntegrityWorkflowContext(settings, collector, model, alerter, auditManager);
        workflow.initialise(context, TEST_COLLECTION);
        workflow.start();

        verify(alerter).integrityFailed(anyString(), eq(TEST_COLLECTION));
        verifyNoMoreInteractions(alerter);

        verify(collector).getChecksums(eq(TEST_COLLECTION), any(), any(), anyString(), anyString(), any(), any(EventHandler.class));
        verifyNoMoreInteractions(collector);

        verify(model).getNumberOfFilesInCollection(eq(TEST_COLLECTION));
        verify(model).getFileIDAtPosition(eq(TEST_COLLECTION), eq(0L));
        verifyNoMoreInteractions(model);

        verify(auditManager).addAuditEvent(eq(TEST_COLLECTION), anyString(), anyString(), anyString(), anyString(), any(), any(), any());
        verifyNoMoreInteractions(auditManager);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testOneComponentFailureAndTwoOtherDisagreeOnChecksum() throws Exception {
        addDescription("Test that the workflow works when both pillars deliver the same checksum.");
        addStep("Prepare for calls to mocks", "");
        when(model.getNumberOfFilesInCollection(anyString())).thenReturn(Long.valueOf(1));
        when(model.getFileIDAtPosition(eq(TEST_COLLECTION), eq(0L))).thenReturn(TEST_FILE_1);

        doAnswer((Answer<Void>) invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            ResultingChecksums res1 = createResultingChecksums((String) invocation.getArguments()[3], "abcdef");
            ResultingChecksums res2 = createResultingChecksums((String) invocation.getArguments()[3], "fedcba");
            ContributorEvent e1 = new ChecksumsCompletePillarEvent(PILLAR_1, TEST_COLLECTION, res1,
                    (ChecksumSpecTYPE) invocation.getArguments()[2], false);
            ContributorEvent e2 = new ChecksumsCompletePillarEvent(PILLAR_2, TEST_COLLECTION, res2,
                    (ChecksumSpecTYPE) invocation.getArguments()[2], false);
            ContributorEvent e3 = new ContributorFailedEvent(PILLAR_3, TEST_COLLECTION, ResponseCode.FAILURE);
            eventHandler.handleEvent(e1);
            eventHandler.handleEvent(e2);
            eventHandler.handleEvent(e3);
            eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "COMPONENT FAILED", Arrays.asList(e1, e2, e3)));
            return null;
        }).when(collector).getChecksums(anyString(), any(), any(), anyString(), anyString(), any(), any(EventHandler.class));

        addStep("Run workflow for checking salted checksum.", "Should send alarm about failure");

        Workflow workflow = new SaltedChecksumWorkflow();
        IntegrityWorkflowContext context = new IntegrityWorkflowContext(settings, collector, model, alerter, auditManager);
        workflow.initialise(context, TEST_COLLECTION);
        workflow.start();

        verify(alerter, times(2)).integrityFailed(anyString(), eq(TEST_COLLECTION));
        verifyNoMoreInteractions(alerter);

        verify(collector).getChecksums(eq(TEST_COLLECTION), any(), any(), anyString(), anyString(), any(), any(EventHandler.class));
        verifyNoMoreInteractions(collector);

        verify(model).getNumberOfFilesInCollection(eq(TEST_COLLECTION));
        verify(model).getFileIDAtPosition(eq(TEST_COLLECTION), eq(0L));
        verifyNoMoreInteractions(model);

        verify(auditManager).addAuditEvent(eq(TEST_COLLECTION), anyString(), anyString(), anyString(), anyString(), any(), any(), any());
        verifyNoMoreInteractions(auditManager);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testInconsistentChecksums() {
        addDescription("Test that the workflow discovers and handles inconsistent checksums");
        addStep("Prepare for calls to mocks", "");
        when(model.getNumberOfFilesInCollection(anyString())).thenReturn(Long.valueOf(1));
        when(model.getFileIDAtPosition(eq(TEST_COLLECTION), eq(0L))).thenReturn(TEST_FILE_1);

        doAnswer((Answer<Void>) invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            ResultingChecksums res1 = createResultingChecksums((String) invocation.getArguments()[3], "abcdef");
            ResultingChecksums res2 = createResultingChecksums((String) invocation.getArguments()[3], "fedcba");
            eventHandler.handleEvent(
                    new ChecksumsCompletePillarEvent(PILLAR_1, TEST_COLLECTION, res1, (ChecksumSpecTYPE) invocation.getArguments()[2],
                            false));
            eventHandler.handleEvent(
                    new ChecksumsCompletePillarEvent(PILLAR_2, TEST_COLLECTION, res2, (ChecksumSpecTYPE) invocation.getArguments()[2],
                            false));
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
            return null;
        }).when(collector).getChecksums(anyString(), any(), any(), anyString(), anyString(), any(), any(EventHandler.class));

        addStep("Run workflow for checking salted checksum.", "Should send alarm about failure");

        Workflow workflow = new SaltedChecksumWorkflow();
        IntegrityWorkflowContext context = new IntegrityWorkflowContext(settings, collector, model, alerter, auditManager);
        workflow.initialise(context, TEST_COLLECTION);
        workflow.start();

        verify(collector).getChecksums(eq(TEST_COLLECTION), any(), any(), anyString(), anyString(), any(), any(EventHandler.class));
        verifyNoMoreInteractions(collector);

        verify(model).getNumberOfFilesInCollection(eq(TEST_COLLECTION));
        verify(model).getFileIDAtPosition(eq(TEST_COLLECTION), eq(0L));
        verifyNoMoreInteractions(model);

        verify(auditManager).addAuditEvent(eq(TEST_COLLECTION), anyString(), anyString(), anyString(),
                anyString(), any(), any(), any());
        verifyNoMoreInteractions(auditManager);

        verify(alerter).integrityFailed(anyString(), eq(TEST_COLLECTION));
        verifyNoMoreInteractions(alerter);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testNoReceivedChecksums() {
        addDescription("Test that the workflow handles the case, when no checksums are received");
        addStep("Prepare for calls to mocks", "");
        when(model.getNumberOfFilesInCollection(anyString())).thenReturn(Long.valueOf(1));
        when(model.getFileIDAtPosition(eq(TEST_COLLECTION), eq(0L))).thenReturn(TEST_FILE_1);

        doAnswer((Answer<Void>) invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "", null));
            return null;
        }).when(collector).getChecksums(anyString(), any(), any(), anyString(), anyString(), any(), any(EventHandler.class));

        addStep("Run workflow for checking salted checksum.", "Should send alarm about failure");

        Workflow workflow = new SaltedChecksumWorkflow();
        IntegrityWorkflowContext context = new IntegrityWorkflowContext(settings, collector, model, alerter, auditManager);
        workflow.initialise(context, TEST_COLLECTION);
        workflow.start();

        verify(collector).getChecksums(eq(TEST_COLLECTION), any(), any(), anyString(), anyString(), any(), any(EventHandler.class));
        verifyNoMoreInteractions(collector);

        verify(model).getNumberOfFilesInCollection(eq(TEST_COLLECTION));
        verify(model).getFileIDAtPosition(eq(TEST_COLLECTION), eq(0L));
        verifyNoMoreInteractions(model);

        verify(auditManager).addAuditEvent(eq(TEST_COLLECTION), anyString(), anyString(), anyString(),
                anyString(), any(), any(), any());
        verifyNoMoreInteractions(auditManager);

        verify(alerter).integrityFailed(anyString(), eq(TEST_COLLECTION));
        verifyNoMoreInteractions(alerter);
    }

    private ResultingChecksums createResultingChecksums(String fileId, String checksum) {
        ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
        csData.setCalculationTimestamp(CalendarUtils.getNow());
        csData.setFileID(fileId);
        try {
            csData.setChecksumValue(Base16Utils.encodeBase16(checksum));
        } catch (DecoderException e) {
            System.err.println(e.getMessage());
        }

        ResultingChecksums res = new ResultingChecksums();
        res.getChecksumDataItems().add(csData);
        return res;
    }
}
