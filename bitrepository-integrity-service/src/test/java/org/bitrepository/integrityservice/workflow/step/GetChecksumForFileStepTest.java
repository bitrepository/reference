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
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.integrityservice.workflow.IntegrityContributors;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Performs the validation of the integrity for the checksums.
 */
public class GetChecksumForFileStepTest extends WorkflowstepTest {
    /**
     * The settings for the tests. Should be instantiated in the setup.
     */
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_PILLAR_2 = "test-pillar-2";
    public static final String TEST_PILLAR_3 = "test-pillar-3";

    public static final String FILE_1 = "test-file-1";
    String TEST_COLLECTION = "test-collection";

    @BeforeMethod(alwaysRun = true)
    public void setup() throws DatatypeConfigurationException {
        super.setup();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(TEST_PILLAR_1);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(TEST_PILLAR_2);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(TEST_PILLAR_3);
        integrityContributors = new IntegrityContributors(Arrays.asList(TEST_PILLAR_1, TEST_PILLAR_2, TEST_PILLAR_3), 0);
    }


    @Test(groups = {"regressiontest", "integritytest"})
    public void testNoResults() throws Exception {
        addDescription("Test step for retrieving the checksum of a single file, when no results are delivered.");
        ChecksumSpecTYPE checksumType = ChecksumUtils.getDefault(settings);

        addStep("Setup mock answers", "");
        doAnswer((Answer<Void>) invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
            return null;
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(), any(EventHandler.class));

        GetChecksumForFileStep step = new GetChecksumForFileStep(collector, alerter, checksumType, FILE_1, settings, TEST_COLLECTION,
                integrityContributors);

        addStep("Validate the checksum results", "Should not have any results");
        step.performStep();

        Assert.assertTrue(step.getResults().isEmpty());
        verifyNoInteractions(alerter);
        verify(collector).getChecksums(anyString(), any(), eq(checksumType), eq(FILE_1), anyString(), any(), any(EventHandler.class));
        verifyNoMoreInteractions(collector);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testFullData() throws Exception {
        addDescription("Test step for retrieving the checksum of a single file, when all three pillars deliver results.");
        ChecksumSpecTYPE checksumType = ChecksumUtils.getDefault(settings);

        addStep("Setup mock answers", "");
        doAnswer((Answer<Void>) invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];

            ResultingChecksums res = createResultingChecksums((String) invocation.getArguments()[3]);
            eventHandler.handleEvent(
                    new ChecksumsCompletePillarEvent(TEST_PILLAR_1, TEST_COLLECTION, res, (ChecksumSpecTYPE) invocation.getArguments()[2],
                            false));
            eventHandler.handleEvent(
                    new ChecksumsCompletePillarEvent(TEST_PILLAR_2, TEST_COLLECTION, res, (ChecksumSpecTYPE) invocation.getArguments()[2],
                            false));
            eventHandler.handleEvent(
                    new ChecksumsCompletePillarEvent(TEST_PILLAR_3, TEST_COLLECTION, res, (ChecksumSpecTYPE) invocation.getArguments()[2],
                            false));
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
            return null;
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(), any(EventHandler.class));

        GetChecksumForFileStep step = new GetChecksumForFileStep(collector, alerter, checksumType, FILE_1, settings, TEST_COLLECTION,
                integrityContributors);

        addStep("Validate the checksum results", "Should have checksum for each pillar.");
        step.performStep();

        Assert.assertFalse(step.getResults().isEmpty());
        Assert.assertEquals(step.getResults().size(), 3);
        Assert.assertTrue(step.getResults().containsKey(TEST_PILLAR_1));
        Assert.assertTrue(step.getResults().containsKey(TEST_PILLAR_2));
        Assert.assertTrue(step.getResults().containsKey(TEST_PILLAR_3));

        verifyNoInteractions(alerter);
        verify(collector).getChecksums(anyString(), any(), eq(checksumType), eq(FILE_1), anyString(), any(), any(EventHandler.class));
        verifyNoMoreInteractions(collector);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testComponentFailure() throws Exception {
        addDescription("Test step for retrieving the checksum of a single file, when one pillar fails.");
        ChecksumSpecTYPE checksumType = ChecksumUtils.getDefault(settings);

        addStep("Setup mock answers", "");
        doAnswer((Answer<Void>) invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];

            ResultingChecksums res = createResultingChecksums((String) invocation.getArguments()[3]);
            ContributorEvent e1 = new ChecksumsCompletePillarEvent(TEST_PILLAR_1, TEST_COLLECTION, res,
                    (ChecksumSpecTYPE) invocation.getArguments()[2], false);
            ContributorEvent e2 = new ChecksumsCompletePillarEvent(TEST_PILLAR_2, TEST_COLLECTION, res,
                    (ChecksumSpecTYPE) invocation.getArguments()[2], false);
            ContributorEvent e3 = new ContributorFailedEvent(TEST_PILLAR_3, TEST_COLLECTION, ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
            eventHandler.handleEvent(e1);
            eventHandler.handleEvent(e2);
            eventHandler.handleEvent(e3);
            eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "COMPONENT FAILED", Arrays.asList(e1, e2, e3)));
            return null;
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(), any(EventHandler.class));

        GetChecksumForFileStep step = new GetChecksumForFileStep(collector, alerter, checksumType, FILE_1, settings, TEST_COLLECTION,
                integrityContributors);

        addStep("Validate the file ids", "Should not have integrity issues.");
        step.performStep();

        Assert.assertFalse(step.getResults().isEmpty());
        Assert.assertEquals(step.getResults().size(), 2);
        Assert.assertTrue(step.getResults().containsKey(TEST_PILLAR_1));
        Assert.assertTrue(step.getResults().containsKey(TEST_PILLAR_2));
        Assert.assertFalse(step.getResults().containsKey(TEST_PILLAR_3));

        verify(alerter).integrityFailed(anyString(), eq(TEST_COLLECTION));
        verifyNoMoreInteractions(alerter);
        verify(collector).getChecksums(anyString(), any(), eq(checksumType), eq(FILE_1), anyString(), any(), any(EventHandler.class));
        verifyNoMoreInteractions(collector);
    }

    private ResultingChecksums createResultingChecksums(String fileId) {
        ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
        csData.setCalculationTimestamp(CalendarUtils.getNow());
        csData.setFileID(fileId);
        try {
            csData.setChecksumValue(Base16Utils.encodeBase16("abcdef"));
        } catch (DecoderException e) {
            System.err.println(e.getMessage());
        }
        ResultingChecksums res = new ResultingChecksums();
        res.getChecksumDataItems().add(csData);
        return res;
    }
}
