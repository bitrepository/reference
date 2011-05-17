package org.bitrepository.protocol;

import org.bitrepository.bitrepositorymessages.GetChecksumsComplete;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.GetFileComplete;
import org.bitrepository.bitrepositorymessages.GetFileIDsComplete;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileComplete;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileResponse;

/**
 * Interface for sending messages.
 *
 * TODO: Should recipient queue be extracted from message, rather than be given as parameter?
 */
public interface MessageSender {
    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, GetChecksumsComplete content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, GetChecksumsRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, GetChecksumsResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, GetFileComplete content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, GetFileIDsComplete content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, GetFileIDsRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, GetFileIDsResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, GetFileRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, GetFileResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, IdentifyPillarsForGetChecksumsResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, IdentifyPillarsForGetChecksumsRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, IdentifyPillarsForGetFileIDsRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, IdentifyPillarsForGetFileIDsResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, IdentifyPillarsForGetFileRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, IdentifyPillarsForGetFileResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, IdentifyPillarsForPutFileResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, IdentifyPillarsForPutFileRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, PutFileComplete content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, PutFileRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     * @return The message ID of the sent message.
     */
    String sendMessage(String destinationId, PutFileResponse content);
}
