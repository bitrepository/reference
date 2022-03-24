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
package org.bitrepository.protocol.message;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureUnit;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;

import java.math.BigInteger;

/**
 * Abstract message factory for use in tests, which are suppose to be subclasses by functionality specific factories.
 */
public abstract class ClientTestMessageFactory extends TestMessageFactory {
    //ToDo All default messages should be converted to be loaded from the ExampleMessageFactory. This means all the
    // default attributes is specified through the example messages and the constants below can be removed
    protected static final String CORRELATION_ID_DEFAULT = "CorrelationID";
    public static final String FILE_ID_DEFAULT = "default-test-file.txt";

    protected static final TimeMeasureUnit TIME_MEASURE_UNIT_DEFAULT =
        TimeMeasureUnit.MILLISECONDS;
    protected static final BigInteger TIME_MEASURE_VALUE_DEFAULT = BigInteger.valueOf(1000L);
    protected static final ResponseCode RESPONSE_CODE_DEFAULT = ResponseCode.OPERATION_ACCEPTED_PROGRESS;
    protected static final ResponseCode IDENTIFY_RESPONSE_CODE_DEFAULT = ResponseCode.IDENTIFICATION_POSITIVE;
    protected static final TimeMeasureTYPE TIME_TO_DELIVER_DEFAULT = new TimeMeasureTYPE();

    //Todo All the static defined constants below should be replaced by create method, to avoid single tests cases
    // break other tests by modifying the shared instances of these constants.
    static {
        TIME_TO_DELIVER_DEFAULT.setTimeMeasureUnit(TIME_MEASURE_UNIT_DEFAULT);
        TIME_TO_DELIVER_DEFAULT.setTimeMeasureValue(TIME_MEASURE_VALUE_DEFAULT);
    }

    protected static final ResponseInfo PROGRESS_INFO_DEFAULT = new ResponseInfo();
    static {
        PROGRESS_INFO_DEFAULT.setResponseCode(RESPONSE_CODE_DEFAULT);
        PROGRESS_INFO_DEFAULT.setResponseText("First test progress response message");
    }

    protected final String clientID;

    public ClientTestMessageFactory(String clientID) {
        this.clientID = clientID;
    }

    protected void setResponseDetails(
            MessageResponse response, MessageRequest request, String componentID, String replyTo) {
        initializeMessageDetails(response);
        response.setCorrelationID(request.getCorrelationID());
        response.setDestination(request.getReplyTo());
        response.setReplyTo(replyTo);
        response.setFrom(componentID);
        response.setResponseInfo(createPositiveIdentificationResponseInfo());
    }

    protected static ResponseInfo createPositiveIdentificationResponseInfo() {
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setResponseCode(IDENTIFY_RESPONSE_CODE_DEFAULT);
        return responseInfo;
    }

    protected static ResponseInfo createCompleteResponseInfo() {
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        return responseInfo;
    }

    protected static ResponseInfo createFailureResponseInfo() {
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setResponseCode(ResponseCode.FAILURE);
        return responseInfo;
    }
}
