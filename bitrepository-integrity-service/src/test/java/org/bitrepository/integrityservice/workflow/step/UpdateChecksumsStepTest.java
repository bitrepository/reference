package org.bitrepository.integrityservice.workflow.step;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.mocks.MockCollector;
import org.bitrepository.integrityservice.mocks.MockIntegrityAlerter;
import org.bitrepository.integrityservice.mocks.MockIntegrityModel;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UpdateChecksumsStepTest extends ExtendedTestCase {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final List<String> PILLAR_IDS = Arrays.asList(TEST_PILLAR_1);
    public static final String TEST_FILE_1 = "test-file-1";
    public static final String DEFAULT_CHECKSUM = "0123456789";
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testPositiveReply() {
        addDescription("Test the step for updating the checksums can handle COMPLETE operation event.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumType,
                    String auditTrailInformation, EventHandler eventHandler) {
                super.getChecksums(pillarIDs, fileIDs, checksumType, auditTrailInformation, eventHandler);
                eventHandler.handleEvent(new ContributorEvent(OperationEventType.COMPLETE, "", TEST_PILLAR_1, "conversationID"));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateChecksumsStep step = new UpdateChecksumsStep(collector, store, alerter, PILLAR_IDS, createChecksumSpecTYPE());
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddChecksums(), 0);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 0);
        Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 1);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testNegativeReply() {
        addDescription("Test the step for updating the checksums can handle FAILURE operation event.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumType,
                    String auditTrailInformation, EventHandler eventHandler) {
                super.getChecksums(pillarIDs, fileIDs, checksumType, auditTrailInformation, eventHandler);
                eventHandler.handleEvent(new ContributorEvent(OperationEventType.FAILED, "", TEST_PILLAR_1, "conversationID"));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateChecksumsStep step = new UpdateChecksumsStep(collector, store, alerter, PILLAR_IDS, createChecksumSpecTYPE());
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddChecksums(), 0);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 1);
        Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 1);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testIngestOfResults() {
        addDescription("Test the step for updating the checksums delivers the results to the integrity model.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumType,
                    String auditTrailInformation, EventHandler eventHandler) {
                super.getChecksums(pillarIDs, fileIDs, checksumType, auditTrailInformation, eventHandler);
                ChecksumsCompletePillarEvent event = new ChecksumsCompletePillarEvent(createResultingChecksums(DEFAULT_CHECKSUM, TEST_FILE_1), 
                        createChecksumSpecTYPE(), TEST_PILLAR_1, "info", "conversationID");
                eventHandler.handleEvent(event);
                
                eventHandler.handleEvent(new ContributorEvent(OperationEventType.COMPLETE, "", TEST_PILLAR_1, "conversationID"));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateChecksumsStep step = new UpdateChecksumsStep(collector, store, alerter, PILLAR_IDS, createChecksumSpecTYPE());
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddChecksums(), 1);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 0);
        Assert.assertEquals(collector.getNumberOfCallsForGetChecksums(), 1);
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
