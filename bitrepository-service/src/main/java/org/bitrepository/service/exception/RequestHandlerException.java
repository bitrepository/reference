/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.service.exception;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;

/**
 * The exception for the request handlers.
 */
@SuppressWarnings("serial")
public abstract class RequestHandlerException extends Exception {
    /**
     * The ResponseInfo wrapped by this exception. Tells the reason for the exception.
     */
    private final ResponseInfo responseInfo;

    /**
     * @param rCode The response code.
     * @param rText The text for the response info.
     */
    public RequestHandlerException(ResponseCode rCode, String rText) {
        super(rText);
        this.responseInfo = new ResponseInfo();
        this.responseInfo.setResponseCode(rCode);
        this.responseInfo.setResponseText(rText);
    }

    /**
     * @param rCode The response code.
     * @param rText The text for the response info.
     * @param e     The exception to wrap into the StackTrace.
     */
    public RequestHandlerException(ResponseCode rCode, String rText, Exception e) {
        super(rText, e);
        this.responseInfo = new ResponseInfo();
        this.responseInfo.setResponseCode(rCode);
        this.responseInfo.setResponseText(rText);
    }

    /**
     * @param rInfo The response info.
     */
    public RequestHandlerException(ResponseInfo rInfo) {
        super(rInfo.getResponseText());
        this.responseInfo = rInfo;
    }

    /**
     * @return The wrapped ResponseInfo.
     */
    public ResponseInfo getResponseInfo() {
        return responseInfo;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + responseInfo.toString();
    }
}
