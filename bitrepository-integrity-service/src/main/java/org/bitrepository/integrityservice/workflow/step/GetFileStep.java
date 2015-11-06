package org.bitrepository.integrityservice.workflow.step;

import java.net.URL;

import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.integrityservice.collector.IntegrityEventCompleteAwaiter;
import org.bitrepository.integrityservice.workflow.IntegrityWorkflowContext;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;

/**
 * Workflow step to perform the GetFile operation.
 */
public class GetFileStep extends AbstractWorkFlowStep {

    /** The context for the integrity workflow.*/
    protected final IntegrityWorkflowContext context;
    /** The collectionID */
    protected final String collectionId;
    /** The id of the file to retrieve.*/
    private final String fileId;
    /** The URL for the GetFile operation to have the file delivered at. */
    private final URL uploadUrl;

    /**
     * Constructor.
     * @param context The context for the workflow.
     * @param collectionId The id of the collection to get the file from.
     * @param fileId The id of the file to get.
     * @param uploadUrl The URL where the file must be delivered.
     */
    public GetFileStep(IntegrityWorkflowContext context, String collectionId, String fileId, URL uploadUrl) {
        this.context = context;
        this.collectionId = collectionId;
        this.fileId = fileId;
        this.uploadUrl = uploadUrl;
    }

    @Override
    public String getName() {
        return "Performing GetFile for '" + fileId + "'.";
    }

    @Override
    public void performStep() {
        IntegrityEventCompleteAwaiter eventHandler = new IntegrityEventCompleteAwaiter(context.getSettings());
        context.getCollector().getFile(collectionId, fileId, uploadUrl, eventHandler, "IntegrityService: " 
                + getName());

        OperationEvent event = eventHandler.getFinish();
        if(event.getEventType() == OperationEventType.FAILED) {
            throw new IllegalStateException("Aborting workflow due to failure getting the file '" + fileId + "'. "
                    + "Cause: " + event.toString());
        }
    }
}
