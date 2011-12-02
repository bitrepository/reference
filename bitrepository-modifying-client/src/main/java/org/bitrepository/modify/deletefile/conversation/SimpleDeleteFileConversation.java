package org.bitrepository.modify.deletefile.conversation;

import java.util.UUID;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.putfile.conversation.IdentifyPillarsForPutFile;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * A conversation for the DeleteFile operation.
 * Logic for behaving sanely in DeleteFile conversations.
 */
public class SimpleDeleteFileConversation extends AbstractConversation {
    /** The sender to use for dispatching messages */
    final MessageSender messageSender;
    /** The configuration specific to the SLA related to this conversion. */
    final Settings settings;
    
    /** The ID of the file which should be deleted. */
    final String fileID;
    /** The ID of the pillar to delete the file from. */
    final String pillarId;
    /** The checksum of the file to delete.*/
    final String checksumOfFileToDelete;
    /** The checksums to request from the pillar.*/
    final ChecksumSpecTYPE checksumSpecOfFileToDelete;
    /** The state of the PutFile transaction.*/
    DeleteFileState conversationState;
    /** The audit trail information for the conversation.*/
    final String auditTrailInformation;

    /**
     * Constructor.
     * Initializes all the variables for the conversation.
     * 
     * @param messageSender The instance to send the messages with.
     * @param settings The settings of the client.
     * @param fileId The id of the file.
     * @param pillarId The id of the pillar.
     * @param checksumOfFileToDelete The checksum of the file to delete.
     * @param checksumSpecOfFileToDelete The checksum specifications for the file to delete.
     * @param eventHandler The event handler.
     * @param flowController The flow controller for the conversation.
     * @param auditTrailInformation The audit trail information for the conversation.
     */
    public SimpleDeleteFileConversation(MessageSender messageSender,
            Settings settings,
            String fileId,
            String pillarId,
            String checksumOfFileToDelete,
            ChecksumSpecTYPE checksumSpecOfFileToDelete,
            EventHandler eventHandler,
            FlowController flowController,
            String auditTrailInformation) {
        super(messageSender, UUID.randomUUID().toString(), eventHandler, flowController);
        
        this.messageSender = messageSender;
        this.settings = settings;
        this.fileID = fileId;
        this.pillarId = pillarId;
        this.checksumOfFileToDelete = checksumOfFileToDelete;
        this.checksumSpecOfFileToDelete = checksumSpecOfFileToDelete;
        this.auditTrailInformation = auditTrailInformation;
        conversationState = new IdentifyPillarsForDeleteFile(this);
    }
    
    @Override
    public synchronized void onMessage(IdentifyPillarsForDeleteFileResponse message) {
        conversationState.onMessage(message);
    }
    
    @Override
    public synchronized void onMessage(DeleteFileProgressResponse message) {
        conversationState.onMessage(message);
    }
    
    @Override
    public synchronized void onMessage(DeleteFileFinalResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public boolean hasEnded() {
        return conversationState instanceof DeleteFileFinished;
    }
    
    @Override
    public ConversationState getConversationState() {
        return conversationState;
    }
    
    @Override
    public void endConversation() {
        conversationState.endConversation();
    }
}
