package org.bitrepository.integrityservice.workflow.step;

import java.net.URL;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.GetFileEventHandler;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.integrityservice.workflow.IntegrityWorkflowContext;
import org.bitrepository.service.exception.WorkflowAbortedException;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;

/**
 * Workflow step to perform the PutFile operation.
 */
public class PutFileStep extends AbstractWorkFlowStep {

    /** The context for the integrity workflow.*/
    protected final IntegrityWorkflowContext context;
    /** The collectionID */
    protected final String collectionId;
    /** The id of the file to put.*/
    private final String fileId;
    /** The URL containing the file for the PutFile operation. */
    private final URL uploadUrl;
    /** The checksum of the file to put.*/
    private final String checksum;

    /**
     * Constructor.
     * @param context The context for the workflow.
     * @param collectionId The id of the collection to put the file to.
     * @param fileId The id of the file to put.
     * @param uploadUrl The URL for the file of the putfile operation.'
     * @param checksum The checksum of the file.
     */
    public PutFileStep(IntegrityWorkflowContext context, String collectionId, String fileId, URL uploadUrl, String checksum) {
        this.context = context;
        this.collectionId = collectionId;
        this.fileId = fileId;
        this.uploadUrl = uploadUrl;
        this.checksum = checksum;
    }

    @Override
    public String getName() {
        return "Performing PutFile for '" + fileId + "'.";
    }

    @Override
    public void performStep() throws WorkflowAbortedException {
        CompleteEventAwaiter eventHandler = new GetFileEventHandler(context.getSettings(), null);
        ChecksumDataForFileTYPE checksumValidationData = new ChecksumDataForFileTYPE();
        checksumValidationData.setCalculationTimestamp(CalendarUtils.getNow());
        checksumValidationData.setChecksumSpec(ChecksumUtils.getDefault(context.getSettings()));
        checksumValidationData.setChecksumValue(Base16Utils.encodeBase16(checksum));
        context.getCollector().putFile(collectionId, fileId, uploadUrl, checksumValidationData, eventHandler, "IntegrityService: " 
                + getName());

        OperationEvent event = eventHandler.getFinish();
        if(event.getEventType() == OperationEventType.FAILED) {
            throw new WorkflowAbortedException("Aborting workflow due to failure putting the file '" + fileId + "'. "
                    + "Cause: " + event.toString());
        }
    }
}
