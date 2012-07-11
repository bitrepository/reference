package org.bitrepository.integrityservice.workflow.step;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.mocks.MockCollector;
import org.bitrepository.integrityservice.mocks.MockIntegrityAlerter;
import org.bitrepository.integrityservice.mocks.MockIntegrityModel;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class UpdateFileIDsStepTest extends ExtendedTestCase {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final List<String> PILLAR_IDS = Arrays.asList(TEST_PILLAR_1);
    public static final String TEST_FILE_1 = "test-file-1";
    public static final String DEFAULT_CHECKSUM = "0123456789";
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testPositiveReply() {
        addDescription("Test the step for updating the file ids can handle COMPLETE operation event.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, String auditTrailInformation,
                    EventHandler eventHandler) {
                super.getFileIDs(pillarIDs, fileIDs, auditTrailInformation, eventHandler);
                eventHandler.handleEvent(new ContributorEvent(OperationEventType.COMPLETE, "", TEST_PILLAR_1, "conversationID"));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateFileIDsStep step = new UpdateFileIDsStep(collector, store, alerter, PILLAR_IDS);
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddFileIDs(), 0);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 0);
        Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 1);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testNegativeReply() {
        addDescription("Test the step for updating the file ids can handle FAILED operation event.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, String auditTrailInformation,
                    EventHandler eventHandler) {
                super.getFileIDs(pillarIDs, fileIDs, auditTrailInformation, eventHandler);
                eventHandler.handleEvent(new ContributorEvent(OperationEventType.FAILED, "", TEST_PILLAR_1, "conversationID"));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateFileIDsStep step = new UpdateFileIDsStep(collector, store, alerter, PILLAR_IDS);
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddFileIDs(), 0);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 1);
        Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 1);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testIngestOfResults() {
        addDescription("Test the step for updating the file ids can ingest the data correctly into the store.");
        MockCollector collector = new MockCollector() {
            @Override
            public void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, String auditTrailInformation,
                    EventHandler eventHandler) {
                super.getFileIDs(pillarIDs, fileIDs, auditTrailInformation, eventHandler);
                eventHandler.handleEvent(new ContributorEvent(OperationEventType.IDENTIFICATION_COMPLETE, "", TEST_PILLAR_1, "conversationID"));
                FileIDsCompletePillarEvent event = new FileIDsCompletePillarEvent(createResultingFileIDs(TEST_FILE_1), TEST_PILLAR_1, "info", "conversationID");
                eventHandler.handleEvent(event);
                
                eventHandler.handleEvent(new ContributorEvent(OperationEventType.COMPLETE, "", TEST_PILLAR_1, "conversationID"));
            }
        };
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        UpdateFileIDsStep step = new UpdateFileIDsStep(collector, store, alerter, PILLAR_IDS);
        
        step.performStep();
        Assert.assertEquals(store.getCallsForAddFileIDs(), 1);
        Assert.assertEquals(alerter.getCallsForOperationFailed(), 0);
        Assert.assertEquals(collector.getNumberOfCallsForGetFileIDs(), 1);
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
}
