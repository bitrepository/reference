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
package org.bitrepository.common.utils;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;

/**
 * Utility class for making default responses.
 */
public class ResponseInfoUtils {
    /** Private constructor to prevent instantiation of this utility class.*/
    private ResponseInfoUtils() { }
    
    /**
     * The default message for identifying a contributor.
     * @return The ResponseInfo for telling that a contributor has been identified.
     */
    public static ResponseInfo getPositiveIdentification() {
        ResponseInfo ri = new ResponseInfo();
        ri.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        ri.setResponseText("Contributor identified for operation.");
        return ri;
    }
    
    /**
     * The default message for the initial progress response for a contributor.
     * @return The ResponseInfo for telling, that the operation has started.
     */
    public static ResponseInfo getInitialProgressResponse() {
        ResponseInfo ri = new ResponseInfo();
        ri.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        ri.setResponseText("Operation acknowledged and accepted.");
        return ri;
    }
}
