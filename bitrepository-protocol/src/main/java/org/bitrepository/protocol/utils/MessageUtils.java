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
import org.bitrepository.bitrepositorymessages.MessageResponse;

public class MessageUtils {
    private MessageUtils() {}

    public static boolean isPositiveIdentifyResponse (MessageResponse response) {
        ResponseCode responseCode = response.getResponseInfo().getResponseCode();
        return responseCode.equals(ResponseCode.IDENTIFICATION_POSITIVE);
    }

    public static boolean isPositiveProgressResponse (MessageResponse response) {
        ResponseCode responseCode = response.getResponseInfo().getResponseCode();
        return responseCode.equals(ResponseCode.IDENTIFICATION_POSITIVE) ||
               responseCode.equals(ResponseCode.OPERATION_ACCEPTED_PROGRESS) ||
               responseCode.equals(ResponseCode.OPERATION_COMPLETED);
    }
    public static boolean isIdentifyResponse (MessageResponse response) {
        ResponseCode responseCode = response.getResponseInfo().getResponseCode();
        return responseCode.equals(ResponseCode.IDENTIFICATION_POSITIVE) ||
               responseCode.equals(ResponseCode.IDENTIFICATION_NEGATIVE);
    }
}
