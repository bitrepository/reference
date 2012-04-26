package org.bitrepository.protocol.message;

import org.bitrepository.bitrepositorymessages.MessageRequest;

/**
 */
public class ContributorTestMessageFactory extends TestMessageFactory {
    protected final String collectionDestination;
    protected final String contributorID;
    protected final String contributorDestination;
    protected final String clientID;
    protected final String clientDestination;

    public ContributorTestMessageFactory(
            String collectionID, String collectionDestination, String contributorID,
            String contributorDestination, String clientID, String clientDestination) {
        super(collectionID);
        this.contributorID = contributorID;
        this.contributorDestination = contributorDestination;
        this.collectionDestination = collectionDestination;
        this.clientID = clientID;
        this.clientDestination = clientDestination;
    }

    protected void initializeRequestDetails(MessageRequest request, String correlationID) {
        initializeMessageDetails(request);
        request.setCorrelationID(correlationID);
        request.setTo(collectionDestination);
        request.setCorrelationID(CORRELATION_ID_DEFAULT);
        request.setFrom(clientID);
        request.setReplyTo(clientDestination);
    }

    protected void initializeIdentifyRequestDetails(MessageRequest identifyRequest) {
        initializeMessageDetails(identifyRequest);
        identifyRequest.setTo(collectionDestination);
        identifyRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        identifyRequest.setFrom(clientID);
        identifyRequest.setReplyTo(clientDestination);
    }
}
