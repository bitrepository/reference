package org.bitrepository.pillar.integration.func.putfile;

import java.net.MalformedURLException;
import java.net.URL;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.putfile.conversation.IdentifyPillarsForPutFile;
import org.bitrepository.modify.putfile.conversation.PutFileConversationContext;
import org.bitrepository.protocol.messagebus.MessageSender;

public class PutFileProtocolAdaptor {
    private final MessageSender messageSender;
    private final Settings settings;

    public PutFileProtocolAdaptor(Settings settings, MessageSender messageSender) {
        this.settings = settings;
        this.messageSender = messageSender;
    }


    /**
     *
     * @return The destination for putFile for the indicated pillar.
     */
    protected String identifyPillarForPut(String pillarID)  {
        PutFileConversationContext context = null;
        try {
            context = new PutFileConversationContext(
                    "dummyFileID", new URL("http://dummyURL"), 10L,
                    null, null,
                    settings, messageSender, "PutFileProtocolAdaptor", null,
                    null
            );
        } catch (MalformedURLException e) {
            // Never happens.
        }
        IdentifyPillarsForPutFile identifier = new IdentifyPillarsForPutFile(context);
        return null;
    }
}
