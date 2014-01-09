package org.bitrepository.protocol.utils;

import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.settings.referencesettings.MessageCategory;

public class MessageCategoryUtils {
    public static MessageCategory getCategory(Message message) {
        if (isCategoryFast(message)) return MessageCategory.FAST;
        else return MessageCategory.SLOW;
    }

    private static boolean isCategoryFast(Message message) {
        String messageType = message.getClass().getSimpleName();
        if (messageType.contains("Identify") ||
            messageType.contains("Progress") ||
            message instanceof GetStatusRequest) {
            return true;
        } else {
            return false;
        }
    }
}
