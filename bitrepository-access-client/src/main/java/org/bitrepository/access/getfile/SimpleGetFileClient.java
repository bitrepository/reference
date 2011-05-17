package org.bitrepository.access.getfile;

import org.bitrepository.access.AccessException;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.protocol.CollectionBasedConversationMediator;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The client for sending and handling 'GetFile' operations.
 * Is able to either retrieve a file from a specific pillar, or to identify how fast each pillar in a given SLA is to
 * retrieve  a specific file and then retrieve it from the fastest pillar.
 * The files are delivered to a preconfigured directory.
 *
 * TODO move all the message generations into separate methods, or use the auto-generated constructors, which Mikis
 * has talked about.
 */
public class SimpleGetFileClient extends CollectionBasedConversationMediator<SimpleGetFileConversation> implements
        GetFileClient {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The queue to talk to. */
    private final String queue;
    /** The ID of the SLA. */
    private String slaID;

    public SimpleGetFileClient(MessageBus messagebus, SimpleGetFileConversationFactory simpleGetFileConversationFactory,
                               String slaID, String queue) {
        super(simpleGetFileConversationFactory, messagebus, queue);
        log.info("Initialising the GetFileClient");

        // TODO: Replace with one parameter denoting the SLA, which can produce the queue
        this.queue = queue;
        this.slaID = slaID;
    }

    @Override
    public void retrieveFastest(String fileID) {
        // validate arguments
        if(fileID == null || fileID.isEmpty()) {
            throw new IllegalArgumentException("The String fileId may not be null or the empty string.");
        }
        log.info("Requesting fastest retrieval of the file '" + fileID + "' which belong to the SLA '" + slaID + "'.");
        // create message requesting delivery time for the given file.
        IdentifyPillarsForGetFileRequest msg = new IdentifyPillarsForGetFileRequest();
        msg.setMinVersion(BigInteger.valueOf(1L));
        msg.setVersion(BigInteger.valueOf(1L));
        msg.setSlaID(slaID);
        msg.setFileID(fileID);
        msg.setReplyTo(queue);

        SimpleGetFileConversation conversation = startConversation();
        conversation.sendMessage(queue, msg);

        // TODO: Should we wait for the conversation to end before returning? How is the result delivered?
    }

    @Override
    public void getFile(String fileID, String pillarID) {
        log.info("Requesting the file '" + fileID + "' from pillar '" + pillarID + "'.");

        URL url;
        try {
            url = ProtocolComponentFactory.getInstance().getFileExchange().getURL(fileID);
        } catch (MalformedURLException e) {
            throw new AccessException("Unable to create file from URL", e);
        }
        GetFileRequest msg = new GetFileRequest();
        msg.setMinVersion(BigInteger.valueOf(1L));
        msg.setVersion(BigInteger.valueOf(1L));
        msg.setSlaID(slaID);
        msg.setFileID(fileID);
        msg.setPillarID(pillarID);
        msg.setReplyTo(queue);
        msg.setFileAddress(url.toExternalForm());

        SimpleGetFileConversation conversation = startConversation();
        conversation.sendMessage(queue, msg);

        // TODO: Should we wait for the conversation to end before returning? How is the result delivered?
    }
}
