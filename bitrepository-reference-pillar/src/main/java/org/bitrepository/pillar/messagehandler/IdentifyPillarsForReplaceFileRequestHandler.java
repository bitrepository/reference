package org.bitrepository.pillar.messagehandler;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.ReferenceArchive;
import org.bitrepository.pillar.exceptions.IdentifyPillarsException;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.time.TimeMeasurementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the ReplaceFile operation.
 */
public class IdentifyPillarsForReplaceFileRequestHandler 
        extends PillarMessageHandler<IdentifyPillarsForReplaceFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Constructor.
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    protected IdentifyPillarsForReplaceFileRequestHandler(Settings settings, MessageBus messageBus,
            AlarmDispatcher alarmDispatcher, ReferenceArchive referenceArchive) {
        super(settings, messageBus, alarmDispatcher, referenceArchive);
    }

    @Override
    void handleMessage(IdentifyPillarsForReplaceFileRequest message) {
        ArgumentValidator.checkNotNull(message, "IdentifyPillarsForDeleteFileRequest message");

        try {
            validateBitrepositoryCollectionId(message.getCollectionID());
            checkThatRequestedFileIsAvailable(message);
            respondSuccesfullIdentification(message);
        } catch (IllegalArgumentException e) {
            alarmDispatcher.handleIllegalArgumentException(e);
        } catch (IdentifyPillarsException e) {
            log.warn("Unsuccessfull identification for the GetChecksums operation.", e);
            respondUnsuccessfulIdentification(message, e);
        } catch (RuntimeException e) {
            alarmDispatcher.handleRuntimeExceptions(e);
        }
    }
    
    /**
     * Validates that the requested files are present in the archive. 
     * Otherwise an {@link IdentifyPillarsException} with the appropriate errorcode is thrown.
     * @param message The message containing the id of the file. If no file id is given, then a warning is logged, 
     * but the operation is accepted.
     */
    public void checkThatRequestedFileIsAvailable(IdentifyPillarsForReplaceFileRequest message) {
        if(!archive.hasFile(message.getFileID())) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND);
            irInfo.setResponseText("Could not find the requested file to delete.");
            throw new IdentifyPillarsException(irInfo);
        }
    }

    /**
     * Method for making a successful response to the identification.
     * @param message The request message to respond to.
     */
    private void respondSuccesfullIdentification(IdentifyPillarsForReplaceFileRequest message) {
        // Create the response.
        IdentifyPillarsForReplaceFileResponse reply = createIdentifyPillarsForReplaceFileResponse(message);
        
        // set the missing variables in the reply:
        // TimeToDeliver, IdentifyResponseInfo (ignore PillarChecksumSpec)
        reply.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
                settings.getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        
        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        reply.setResponseInfo(irInfo);
        
        messagebus.sendMessage(reply);
    }
    
    /**
     * Sends a bad response with the given cause.
     * @param message The identification request to respond to.
     * @param cause The cause of the bad identification (e.g. which file is missing).
     */
    private void respondUnsuccessfulIdentification(IdentifyPillarsForReplaceFileRequest message, 
            IdentifyPillarsException cause) {
        IdentifyPillarsForReplaceFileResponse reply = createIdentifyPillarsForReplaceFileResponse(message);
        
        reply.setTimeToDeliver(TimeMeasurementUtils.getMaximumTime());
        reply.setResponseInfo(cause.getResponseInfo());
        
        messagebus.sendMessage(reply);
    }
    
    /**
     * Creates a IdentifyPillarsForGetChecksumsResponse based on a 
     * IdentifyPillarsForReplaceFileRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - ResponseInfo
     * <br/> - PillarChecksumSpec
     * 
     * @param msg The IdentifyPillarsForReplaceFileRequest to base the response on.
     * @return The response to the request.
     */
    private IdentifyPillarsForReplaceFileResponse createIdentifyPillarsForReplaceFileResponse(
            IdentifyPillarsForReplaceFileRequest msg) {
        IdentifyPillarsForReplaceFileResponse res = new IdentifyPillarsForReplaceFileResponse();
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCorrelationID(msg.getCorrelationID());
        res.setFileID(msg.getFileID());
        res.setTo(msg.getReplyTo());
        res.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        res.setCollectionID(settings.getCollectionID());
        res.setReplyTo(settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        
        return res;
    }
}
