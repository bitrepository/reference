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
package org.bitrepository.integrityservice.workflow;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.checking.reports.ChecksumReportModel;
import org.bitrepository.integrityservice.checking.reports.MissingChecksumReportModel;
import org.bitrepository.integrityservice.checking.reports.MissingFileReportModel;
import org.bitrepository.integrityservice.checking.reports.ObsoleteChecksumReportModel;
import org.bitrepository.integrityservice.mocks.MockChecker;
import org.bitrepository.integrityservice.mocks.MockCollector;
import org.bitrepository.integrityservice.mocks.MockIntegrityAlerter;
import org.bitrepository.integrityservice.mocks.MockIntegrityModel;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class BasicIntegrityWorkflowTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;

    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_FILE_1 = "test-file-1";
    public static final String DEFAULT_CHECKSUM = "0123456789";

    public static final Long DEFAULT_TIMEOUT = 60000L;

    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");
        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(TEST_PILLAR_1);
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(0L);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testGoodCaseWorkflow() {
        addDescription("Test the good case, when every step goes well.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumType,
                    String auditTrailInformation, EventHandler eventHandler) {
                super.getChecksums(pillarIDs, fileIDs, checksumType, auditTrailInformation, eventHandler);
                eventHandler.handleEvent(new CompleteEvent(null));
            }
            @Override
            public void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, String auditTrailInformation,
                    EventHandler eventHandler) {
                super.getFileIDs(pillarIDs, fileIDs, auditTrailInformation, eventHandler);
                eventHandler.handleEvent(new CompleteEvent(null));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(settings.getCollectionSettings().getClientSettings().getPillarIDs()));
        MockChecker checker = new MockChecker();
        BasicIntegrityWorkflow workflow = new BasicIntegrityWorkflow(settings, collector, store, checker, alerter);

        workflow.start();

        Assert.assertEquals(store.getCallsForAddFileIDs(), 0);
        Assert.assertEquals(store.getCallsForAddChecksums(), 0);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 0);
        Assert.assertEquals(alerter.getCallsForIntegrityFailed(), 0);
        Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 1);
        Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 1);
        Assert.assertEquals(checker.getCallsForCheckMissingChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckObsoleteChecksums(), 1);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testBadCaseWorkflow() {
        addDescription("Test the bad case, when every step goes wrong.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumType,
                    String auditTrailInformation, EventHandler eventHandler) {
                super.getChecksums(pillarIDs, fileIDs, checksumType, auditTrailInformation, eventHandler);
                eventHandler.handleEvent(new OperationFailedEvent("Failed", null));
            }
            @Override
            public void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, String auditTrailInformation,
                    EventHandler eventHandler) {
                super.getFileIDs(pillarIDs, fileIDs, auditTrailInformation, eventHandler);
                eventHandler.handleEvent(new OperationFailedEvent("Failed", null));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(settings.getCollectionSettings().getClientSettings().getPillarIDs()));
        MockChecker checker = new MockChecker() {
            @Override
            public ObsoleteChecksumReportModel checkObsoleteChecksums(long outdatedInterval) {
                ObsoleteChecksumReportModel res = super.checkObsoleteChecksums(outdatedInterval);
                res.reportObsoleteChecksum(TEST_FILE_1, TEST_PILLAR_1, CalendarUtils.getEpoch());
                return res;
            }
            @Override
            public MissingChecksumReportModel checkMissingChecksums() {
                MissingChecksumReportModel res = super.checkMissingChecksums();
                res.reportMissingChecksum(TEST_FILE_1, Arrays.asList(TEST_PILLAR_1));
                return res;
            }
            @Override
            public ChecksumReportModel checkChecksum() {
                ChecksumReportModel res = super.checkChecksum();
                res.reportChecksumIssue(TEST_FILE_1, TEST_PILLAR_1, DEFAULT_CHECKSUM);
                return res;
            }
            @Override
            public MissingFileReportModel checkFileIDs(FileIDs fileIDs) {
                MissingFileReportModel res = super.checkFileIDs(fileIDs);
                res.reportMissingFile(TEST_FILE_1, Arrays.asList(TEST_PILLAR_1));
                return res;
            }
        };

        BasicIntegrityWorkflow workflow = new BasicIntegrityWorkflow(settings, collector, store, checker, alerter);

        workflow.start();

        Assert.assertEquals(store.getCallsForAddFileIDs(), 0);
        Assert.assertEquals(store.getCallsForAddChecksums(), 0);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 2);
        Assert.assertEquals(alerter.getCallsForIntegrityFailed(), 4);
        Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 1);
        Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 1);
        Assert.assertEquals(checker.getCallsForCheckMissingChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckObsoleteChecksums(), 1);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testIngestThroughWorkflow() {
        addDescription("Test the good case, when every step goes well.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumType,
                    String auditTrailInformation, EventHandler eventHandler) {
                super.getChecksums(pillarIDs, fileIDs, checksumType, auditTrailInformation, eventHandler);
                ChecksumsCompletePillarEvent event = new ChecksumsCompletePillarEvent(
                        TEST_PILLAR_1, createResultingChecksums(DEFAULT_CHECKSUM, TEST_FILE_1), createChecksumSpecTYPE());
                eventHandler.handleEvent(event);
                eventHandler.handleEvent(new CompleteEvent(null));
            }
            @Override
            public void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, String auditTrailInformation,
                    EventHandler eventHandler) {
                super.getFileIDs(pillarIDs, fileIDs, auditTrailInformation, eventHandler);
                FileIDsCompletePillarEvent event = new FileIDsCompletePillarEvent(
                        TEST_PILLAR_1, createResultingFileIDs(TEST_FILE_1));
                eventHandler.handleEvent(event);

                eventHandler.handleEvent(new CompleteEvent(null));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(settings.getCollectionSettings().getClientSettings().getPillarIDs()));
        MockChecker checker = new MockChecker();
        BasicIntegrityWorkflow workflow = new BasicIntegrityWorkflow(settings, collector, store, checker, alerter);

        workflow.start();

        Assert.assertEquals(store.getCallsForAddFileIDs(), 1);
        Assert.assertEquals(store.getCallsForAddChecksums(), 1);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 0);
        Assert.assertEquals(alerter.getCallsForIntegrityFailed(), 0);
        Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 1);
        Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 1);
        Assert.assertEquals(checker.getCallsForCheckMissingChecksums(), 1);
        Assert.assertEquals(checker.getCallsForCheckObsoleteChecksums(), 1);
    }

    private ResultingFileIDs createResultingFileIDs(String ... fileIds) {
        ResultingFileIDs res = new ResultingFileIDs();
        res.setFileIDsData(getFileIDsData(fileIds));
        return res;
    }

    private FileIDsData getFileIDsData(String... fileIds) {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();

        for(String fileId : fileIds) {
            FileIDsDataItem dataItem = new FileIDsDataItem();
            dataItem.setFileID(fileId);
            dataItem.setFileSize(BigInteger.valueOf(items.getFileIDsDataItem().size() + 1));
            dataItem.setLastModificationTime(CalendarUtils.getNow());
            items.getFileIDsDataItem().add(dataItem);
        }

        res.setFileIDsDataItems(items);
        return res;
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
