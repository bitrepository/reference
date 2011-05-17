package org.bitrepository.access.getfile;

import org.bitrepository.protocol.ConversationFactory;
import org.bitrepository.protocol.MessageBus;

/** Factory that generates GetFile conversations. */
public class SimpleGetFileConversationFactory implements ConversationFactory<SimpleGetFileConversation> {
    /** The message bus used by the conversations to communicate. */
    private final MessageBus messageBus;
    /** The expected numbers of pillars involved in the GetFile conversations. */
    private final int expectedNumberOfPillars;
    /** The default timeout for GetFile operations. May be overridden by better data. */
    private long getFileDefaultTimeout;
    /**The directory where retrieved files are stored. */
    private String fileDir;

    /**
     * Initialise a factory that generates GetFile conversations.
     * @param messageBus The message bus used by the conversations to communicate.
     * @param expectedNumberOfPillars The expected numbers of pillars involved in the GetFile conversations.
     * @param getFileDefaultTimeout The default timeout for GetFile operations. May be overridden by better data.
     * @param fileDir The directory where retrieved files are stored.
     */
    public SimpleGetFileConversationFactory(MessageBus messageBus, int expectedNumberOfPillars,
                                            long getFileDefaultTimeout, String fileDir) {
        this.messageBus = messageBus;
        this.fileDir = fileDir;
        this.expectedNumberOfPillars = expectedNumberOfPillars;
        this.getFileDefaultTimeout = getFileDefaultTimeout;
    }

    @Override
    public SimpleGetFileConversation createConversation() {
        return new SimpleGetFileConversation(messageBus, expectedNumberOfPillars, getFileDefaultTimeout, fileDir);
    }
}
