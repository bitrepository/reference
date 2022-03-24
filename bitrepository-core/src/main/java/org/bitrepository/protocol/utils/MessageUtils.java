/*
 * #%L
 * Bitrepository Protocol
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.protocol.utils;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageResponse;

/**
 * Utility class for validating message responses.
 */
public class MessageUtils {
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private MessageUtils() {
    }

    /**
     * Validates whether a response message has a positive identification.
     *
     * @param response The response message to validate.
     * @return Whether it is positive identified.
     */
    public static boolean isPositiveIdentifyResponse(MessageResponse response) {
        ResponseCode responseCode = response.getResponseInfo().getResponseCode();
        return responseCode.equals(ResponseCode.IDENTIFICATION_POSITIVE);
    }

    /**
     * Validates whether a response message has a positive progress response.
     *
     * @param response The response message to validate.
     * @return Whether it is positive progress response.
     */
    public static boolean isPositiveProgressResponse(MessageResponse response) {
        ResponseCode responseCode = response.getResponseInfo().getResponseCode();
        return responseCode.equals(ResponseCode.IDENTIFICATION_POSITIVE) || responseCode.equals(ResponseCode.OPERATION_ACCEPTED_PROGRESS) ||
                responseCode.equals(ResponseCode.OPERATION_PROGRESS);
    }

    /**
     * Validates whether a response message has an identification response.
     *
     * @param response The response message to validate.
     * @return Whether it is a identification response.
     */
    public static boolean isIdentifyResponse(MessageResponse response) {
        ResponseCode responseCode = response.getResponseInfo().getResponseCode();
        return responseCode.equals(ResponseCode.IDENTIFICATION_POSITIVE) || responseCode.equals(ResponseCode.IDENTIFICATION_NEGATIVE);
    }

    /**
     * Chechs whether the supplied message is an identify request
     *
     * @param message The request message to check.
     * @return Whether it is a identification request.
     */
    public static boolean isIdentifyRequest(Message message) {
        String simpleName = message.getClass().getSimpleName();
        return simpleName.contains("Identify") && simpleName.contains("Request");
    }

    /**
     * @param response the supplied message
     * @return whether the supplied message can be considered a end response for a primitive, emg. ends a series of
     * identify or operation responses.
     */
    public static boolean isEndMessageForPrimitive(MessageResponse response) {
        ResponseCode responseCode = response.getResponseInfo().getResponseCode();
        return !(responseCode.equals(ResponseCode.OPERATION_PROGRESS) || responseCode.equals(ResponseCode.OPERATION_ACCEPTED_PROGRESS));
    }

    /**
     * @param message The message to create an identifier for.
     * @return The identifier based on the message type and a short representation of the correlation ID.
     * @see #getShortConversationID(String)
     */
    public static String createMessageIdentifier(Message message) {
        return message.getClass().getSimpleName() + "(" + getShortConversationID(message.getCorrelationID()) + ")";
    }


    /**
     * @param fullConversationID The full conversation ID to create a short-end version for.
     * @return A shorted conversationID. Only the first part up til the first '-' is used
     * (but at least 4 long).
     */
    public static String getShortConversationID(String fullConversationID) {
        if (fullConversationID.length() > 4) {
            if (fullConversationID.contains("-")) {
                return fullConversationID.substring(0, fullConversationID.indexOf("-", 4));
            } else {
                return fullConversationID.substring(0, 5);
            }
        } else {
            return fullConversationID;
        }
    }
}
