package org.bitrepository.pillar.integration.perf;

import java.util.Collection;
import java.util.List;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.deletefile.DeleteFileClient;
import org.bitrepository.modify.putfile.BlockingPutFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.pillar.integration.TestFileHelper;
import org.bitrepository.protocol.fileexchange.HttpServerConnector;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.testng.Assert;

public class CollectionTestHelper {
    private final Settings settings;
    private final HttpServerConnector httpServer;
    private final SecurityManager securityManager;

    private final GetFileIDsClient getFileIDsClient;
    private final DeleteFileClient deleteFileClient;
    protected PutFileClient putClient;

    public CollectionTestHelper(
            Settings settings,
            HttpServerConnector httpServer) {
        this.settings = settings;
        this.securityManager = new DummySecurityManager();
        this.httpServer = httpServer;

        putClient = ModifyComponentFactory.getInstance().retrievePutClient(
                settings, new DummySecurityManager(), settings.getComponentID()
        );
        getFileIDsClient = AccessComponentFactory.getInstance().createGetFileIDsClient(
                settings, securityManager, settings.getComponentID()
        );
        deleteFileClient = ModifyComponentFactory.getInstance().retrieveDeleteFileClient(
                settings, securityManager, settings.getComponentID()
        );
    }

    public void cleanCollection(Collection<String> pillarIDs) {
        getFileIDsClient.getFileIDs(
                pillarIDs, null, null, new EventHandler() {
            @Override
            public void handleEvent(OperationEvent event) {
                if (event.getType().equals(OperationEvent.OperationEventType.COMPONENT_COMPLETE)) {
                    FileIDsCompletePillarEvent result = (FileIDsCompletePillarEvent) event;
                    Assert.assertEquals(event.getType(), OperationEvent.OperationEventType.COMPONENT_COMPLETE);
                    ResultingFileIDs resFileIDs = result.getFileIDs();
                    deleteAllFilesOnPillar(
                            resFileIDs.getFileIDsData().getFileIDsDataItems().getFileIDsDataItem(),
                            result.getContributorID());
                } else if (event.getType().equals(OperationEvent.OperationEventType.FAILED)) {
                    throw new RuntimeException("Unable to clean repository, " + event.getInfo());
                }
            }
        },
                "Requesting fileIDS for cleanup"
        );
    }

    /**
     * Deletes the files one by one, in a new thread.
     *
     * Doesn't currently support mandatory checksums in the deleteRequests.
     *
     * @param fileIDsItems
     */
    private void deleteAllFilesOnPillar(final List<FileIDsDataItem> fileIDsItems, final String pillarID) {
        Thread thread = new Thread(new Runnable() {
            public void run() {
                for (FileIDsDataItem fileIDsDataItem: fileIDsItems) {
                    ChecksumDataForFileTYPE checksumData = null;
                    if (settings.getCollectionSettings().getProtocolSettings().isRequireChecksumForDestructiveRequests()) {
                        // ToDo retrive checksums.
                    }
                deleteFileClient.deleteFile(fileIDsDataItem.getFileID(), pillarID, checksumData,
                        null, null, "");
                }
            }
        });
        thread.start();
    }

    /**
     *
     * @return The list of created fileIDs.
     */
    private String[] putFiles(int numberToCreate, String prefix) {
        BlockingPutFileClient blockingPutFileClient = new BlockingPutFileClient(putClient);
        String[] fileIDs = TestFileHelper.createFileIDs(numberToCreate , "singleTreadedPutTest");
        for (String fileID:fileIDs) {
            try {
                blockingPutFileClient.putFile(httpServer.getURL(TestFileHelper.DEFAULT_FILE_ID), fileID, 10L,
                        TestFileHelper.getDefaultFileChecksum(), null, null, "singleTreadedPut stress test file");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return fileIDs;
    }

    public void shutdown() {
//        getFileIDsClient.shutdown();
//        deleteFileClient.shutdown();
    }
}
