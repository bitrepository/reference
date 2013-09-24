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
package org.bitrepository.integrityservice.workflow.step;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import access.ContributorQuery;
import access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.IdentificationCompleteEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

public class UpdateChecksumsStepTest extends WorkflowstepTest {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_FILE_1 = "test-file-1";
    public static final String DEFAULT_CHECKSUM = "0123456789";

    @Test(groups = {"regressiontest"})
    public void testPositiveReply() {
        addDescription("Test the step for updating the checksums can handle COMPLETE operation event.");
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[5];
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), Matchers.<Collection<String>>any(), any(ChecksumSpecTYPE.class), anyString(),
                any(ContributorQuery[].class), any(EventHandler.class));

        UpdateChecksumsStep step = new UpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(), settings, TEST_COLLECTION);
        step.performStep();
        verify(collector).getChecksums(eq(TEST_COLLECTION), Matchers.<Collection<String>>any(), any(ChecksumSpecTYPE.class), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verifyNoMoreInteractions(alerter);
    }

    @Test(groups = {"regressiontest"})
    public void testNegativeReply() {
        addDescription("Test the step for updating the checksums can handle FAILURE operation event.");
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[5];
                eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "Problem encountered", null));
                return null;
            }
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), Matchers.<Collection<String>>any(), any(ChecksumSpecTYPE.class), anyString(),
                any(ContributorQuery[].class), any(EventHandler.class));

        UpdateChecksumsStep step = new UpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(), settings, TEST_COLLECTION);
        step.performStep();
        verify(collector).getChecksums(eq(TEST_COLLECTION), Matchers.<Collection<String>>any(), any(ChecksumSpecTYPE.class), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verify(alerter).operationFailed(anyString(), eq(TEST_COLLECTION));
    }
    
    @Test(groups = {"regressiontest"})
    public void testIngestOfResults() {
        addDescription("Test the step for updating the checksums delivers the results to the integrity model.");
        final ResultingChecksums resultingChecksums = createResultingChecksums(DEFAULT_CHECKSUM, TEST_FILE_1);
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[5];
                eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(TEST_PILLAR_1)));
                eventHandler.handleEvent(new ChecksumsCompletePillarEvent(TEST_PILLAR_1, TEST_COLLECTION,
                        resultingChecksums, createChecksumSpecTYPE(), false));
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), Matchers.<Collection<String>>any(), any(ChecksumSpecTYPE.class), anyString(),
                any(ContributorQuery[].class), any(EventHandler.class));

        UpdateChecksumsStep step = new UpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(), settings, TEST_COLLECTION);
        step.performStep();
        verify(collector).getChecksums(eq(TEST_COLLECTION), Matchers.<Collection<String>>any(),
                any(ChecksumSpecTYPE.class), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verify(model).addChecksums(resultingChecksums.getChecksumDataItems(), TEST_PILLAR_1, TEST_COLLECTION);
        verifyNoMoreInteractions(alerter);
    }
    
    @Test(groups = {"regressiontest"})
    public void testPartialResults() {
        addDescription("Test that the number of partial is used for generating more than one request.");
        final ResultingChecksums resultingChecksums = createResultingChecksums(DEFAULT_CHECKSUM, TEST_FILE_1);

        addStep("Setup the collector mock to generate a isPartialResult=true event the first time and a " +
                "isPartialResult=false the second time",
                "The collectors getFileIDs should be called 2 time. The same goes for the 'models' addFileIDs");
        Answer callbackAnswer = new Answer() {
            boolean firstPage = true;
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[5];
                eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(TEST_PILLAR_1)));
                eventHandler.handleEvent(new ChecksumsCompletePillarEvent(TEST_PILLAR_1, TEST_COLLECTION,
                        resultingChecksums, createChecksumSpecTYPE(), firstPage));
                firstPage = false;
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        };
        doAnswer(callbackAnswer).when(collector).getChecksums(
                eq(TEST_COLLECTION), Matchers.<Collection<String>>any(), any(ChecksumSpecTYPE.class), anyString(),
                any(ContributorQuery[].class), any(EventHandler.class));

        UpdateChecksumsStep step = new UpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(), settings, TEST_COLLECTION);

        step.performStep();
        verify(model, times(2)).addChecksums(resultingChecksums.getChecksumDataItems(), TEST_PILLAR_1, TEST_COLLECTION);
        verify(collector, times(2)).getChecksums(eq(TEST_COLLECTION), Matchers.<Collection<String>>any(),
                any(ChecksumSpecTYPE.class), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
    }
    
    private class TestEventHandler implements Runnable {
        final EventHandler eventHandler;
        int partialsLeft;
        TestEventHandler(EventHandler eventhandler, int numberOfPartialCompletes) {
            this.eventHandler = eventhandler;
            this.partialsLeft = numberOfPartialCompletes; 
        }
        @Override
        public void run() {
            synchronized(this) {
                eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(TEST_PILLAR_1)));
                
                ChecksumsCompletePillarEvent event;
                if(partialsLeft > 0) {
                    event = new ChecksumsCompletePillarEvent(
                        TEST_PILLAR_1, TEST_COLLECTION, createResultingChecksums(DEFAULT_CHECKSUM, TEST_FILE_1),
                        createChecksumSpecTYPE(), true);
                } else {
                    event = new ChecksumsCompletePillarEvent(
                            TEST_PILLAR_1, TEST_COLLECTION, createResultingChecksums(DEFAULT_CHECKSUM, TEST_FILE_1),
                            createChecksumSpecTYPE(), false);                    
                }
                eventHandler.handleEvent(event);
                
                eventHandler.handleEvent(new ContributorEvent(TEST_PILLAR_1, TEST_COLLECTION));
            }
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
        }
    }

    private ResultingChecksums createResultingChecksums(String checksum, String ... fileids) {
        ResultingChecksums res = new ResultingChecksums();
        res.getChecksumDataItems().addAll(createChecksumData(checksum, fileids));
        return res;
    }
    
    private List<ChecksumDataForChecksumSpecTYPE> createChecksumData(String checksum, String ... fileids) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        for(String fileId : fileids) {
            ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
            csData.setCalculationTimestamp(CalendarUtils.getNow());
            csData.setChecksumValue(Base16Utils.encodeBase16(checksum));
            csData.setFileID(fileId);
            res.add(csData);
        }
        return res;
    }

    private ChecksumSpecTYPE createChecksumSpecTYPE() {
        ChecksumSpecTYPE res = new ChecksumSpecTYPE();
        res.setChecksumType(ChecksumType.MD5);
        return res;
    }
}
