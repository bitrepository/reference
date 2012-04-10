package org.bitrepository.protocol.conversation;

import java.util.UUID;

public class ConversationIDGenerator {
    public static String generateConversationID() {
        return UUID.randomUUID().toString();
    }
}
