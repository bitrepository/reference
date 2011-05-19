/*
 * #%L
 * Bitrepository Protocol
 * *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol;

import org.bitrepository.bitrepositoryelements.*;
import org.bitrepository.bitrepositorymessages.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates test messages for use in test.
 */
public class TestMessageFactory {

    private static final String CORRELATION_ID_DEFAULT = "CorrelationID";
    private static final String SLA_ID_DEFAULT = "SlaID";
    private static final String REPLY_TO_DEFAULT = "ReplyTo";
    private static final String FILE_ID_DEFAULT = "FileID";
    private static final BigInteger VERSION_DEFAULT = BigInteger.valueOf(1L);

    private static final String TIME_MEASURE_UNIT_DEFAULT = "MILLISECONDS";
    private static final BigInteger TIME_MEASURE_VALUE_DEFAULT = BigInteger.valueOf(1000L);
    private static final String RESPONSE_CODE_DEFAULT = "460";
    private static final String RESPONSE_TEXT_DEFAULT = "Message request has been received and is expected to be met successfully";
    private static final String COMPLETE_CODE_DEFAULT = "480";
    private static final String COMPLETE_TEXT_DEFAULT = "successful completion";

    /**
     * Prevent initialization - currently a utility class.
     */
    private TestMessageFactory() {}

    /**
     * Generate a test message with dummy values.
     *
     * @return A valid but arbitrary message.
     */
    public static IdentifyPillarsForGetFileRequest getTestMessage() {
        IdentifyPillarsForGetFileRequest identifyPillarsForGetFileRequest
                = new IdentifyPillarsForGetFileRequest();
        identifyPillarsForGetFileRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        identifyPillarsForGetFileRequest.setFileID(FILE_ID_DEFAULT);
        identifyPillarsForGetFileRequest.setMinVersion(VERSION_DEFAULT);
        identifyPillarsForGetFileRequest.setReplyTo(REPLY_TO_DEFAULT);
        identifyPillarsForGetFileRequest.setSlaID(SLA_ID_DEFAULT);
        identifyPillarsForGetFileRequest.setVersion(VERSION_DEFAULT);
        return identifyPillarsForGetFileRequest;
    }

    /**
     * Generate IdentifyPillarsForGetFileIDsRequest test message with default values.
     * @return test message
     */
    public static IdentifyPillarsForGetFileIDsRequest getIdentifyPillarsForGetFileIDsRequestTestMessage() {
        return getIdentifyPillarsForGetFileIDsRequestTestMessage(
                CORRELATION_ID_DEFAULT, SLA_ID_DEFAULT, REPLY_TO_DEFAULT, new ArrayList<String>());
    }
    // TODO queue in all methods?
    /**
     * Generate IdentifyPillarsForGetFileIDsRequest test message with specified values.
     * @param correlationID
     * @param slaID
     * @param replyTo
     * @param fileIDlist
     * @return test message
     */
    public static IdentifyPillarsForGetFileIDsRequest getIdentifyPillarsForGetFileIDsRequestTestMessage(
            String correlationID, String slaID, String replyTo, List<String> fileIDlist) {
        IdentifyPillarsForGetFileIDsRequest request = new IdentifyPillarsForGetFileIDsRequest();
        request.setCorrelationID(correlationID);
        request.setSlaID(slaID);
        request.setReplyTo(replyTo);
        FileIDs fileIDs = new FileIDs();
        request.setFileIDs(fileIDs);
        request.setVersion(VERSION_DEFAULT);
        request.setMinVersion(VERSION_DEFAULT);
        return request;
    }
    /**
     * Generate IdentifyPillarsForGetFileIDsResponse test message with default values.
     * @param pillarID
     * @return test message
     */

    public static IdentifyPillarsForGetFileIDsResponse getIdentifyPillarsForGetFileIDsResponseTestMessage(
            String pillarID) {
        return getIdentifyPillarsForGetFileIDsResponseTestMessage(
                CORRELATION_ID_DEFAULT, SLA_ID_DEFAULT, REPLY_TO_DEFAULT, pillarID, new FileIDs(),
                TIME_MEASURE_UNIT_DEFAULT, TIME_MEASURE_VALUE_DEFAULT);
    }
    /**
     * Generate IdentifyPillarsForGetFileIDsResponse test message with specified values.
     * @param correlationID
     * @param slaID
     * @param replyTo
     * @param pillarID
     * @param fileIDs
     * @param timeMeasureUnit
     * @param timeMeasureValue
     * @return test message
     */
    public static IdentifyPillarsForGetFileIDsResponse getIdentifyPillarsForGetFileIDsResponseTestMessage(
            String correlationID, String slaID, String replyTo, String pillarID, FileIDs fileIDs,
            String timeMeasureUnit, BigInteger timeMeasureValue) {
        IdentifyPillarsForGetFileIDsResponse response = new IdentifyPillarsForGetFileIDsResponse();
        response.setCorrelationID(correlationID);
        response.setSlaID(slaID);
        response.setReplyTo(replyTo);
        response.setPillarID(pillarID);
        // todo how do I add a fileID to fileIDs?
        response.setFileIDs(fileIDs);

        TimeMeasureTYPE time = new TimeMeasureTYPE();
        time.setTimeMeasureUnit(timeMeasureUnit);
        time.setTimeMeasureValue(timeMeasureValue);
        response.setTimeToDeliver(time);

        response.setVersion(VERSION_DEFAULT);
        response.setMinVersion(VERSION_DEFAULT);
        return response;
    }
    /**
     * Generate GetFileIDsRequest test message with default values.
     * @param pillarID
     * @return test message
     */

    public static GetFileIDsRequest getGetFileIDsRequestTestMessage(String pillarID) {
        return getGetFileIDsRequestTestMessage(
                CORRELATION_ID_DEFAULT, SLA_ID_DEFAULT, REPLY_TO_DEFAULT, pillarID, new FileIDs());
    }
    /**
     * Generate GetFileIDsRequest test message with specified values.
     *
     * @param correlationID
     * @param slaID
     * @param replyTo
     * @param pillarID
     * @param fileIDs
     * @return test message
     */
    public static GetFileIDsRequest getGetFileIDsRequestTestMessage(
            String correlationID, String slaID, String replyTo, String pillarID, FileIDs fileIDs) {
        GetFileIDsRequest request = new GetFileIDsRequest();
        request.setCorrelationID(correlationID);
        request.setSlaID(slaID);
        request.setReplyTo(replyTo);
        request.setPillarID(pillarID);
        request.setFileIDs(fileIDs);

        request.setVersion(VERSION_DEFAULT);
        request.setMinVersion(VERSION_DEFAULT);
        return request;
    }
    /**
     * Generate GetFileIDsResponse test message with default values.
     * @param pillarID
     * @return test message
     */

    public static GetFileIDsResponse getGetFileIDsResponseTestMessage(String pillarID) {
        return getGetFileIDsResponseTestMessage(
                CORRELATION_ID_DEFAULT, SLA_ID_DEFAULT, REPLY_TO_DEFAULT, pillarID, new FileIDs(),
                RESPONSE_CODE_DEFAULT, RESPONSE_TEXT_DEFAULT);
    }
    /**
     * Generate GetFileIDsResponse test message with specified values.
     * @param correlationID
     * @param slaID
     * @param replyTo
     * @param pillarID
     * @param fileIDs
     * @param responseCode
     * @param responseText
     * @return test message
     */
    public static GetFileIDsResponse getGetFileIDsResponseTestMessage(
            String correlationID, String slaID, String replyTo, String pillarID, FileIDs fileIDs,
            String responseCode, String responseText) {
        GetFileIDsResponse response = new GetFileIDsResponse();
        response.setCorrelationID(correlationID);
        response.setSlaID(slaID);
        response.setReplyTo(replyTo);
        response.setPillarID(pillarID);
        response.setFileIDs(fileIDs);

        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setResponseCode(responseCode);
        responseInfo.setResponseText(responseText);
        response.setResponseInfo(responseInfo);

        response.setVersion(VERSION_DEFAULT);
        response.setMinVersion(VERSION_DEFAULT);
        return response;
    }

    /**
     * Generate GetFileIDsComplete test message with default values.
     * @param pillarID
     * @return test message
     */
    public static GetFileIDsComplete getGetFileIDsCompleteTestMessage(String pillarID) {
        return getGetFileIDsCompleteTestMessage(
                CORRELATION_ID_DEFAULT, SLA_ID_DEFAULT, REPLY_TO_DEFAULT, pillarID, new FileIDs(),
                COMPLETE_CODE_DEFAULT, COMPLETE_TEXT_DEFAULT, new ResultingFileIDs());
    }
    /**
     * Generate GetFileIDsComplete test message with specified values.
     * @param correlationID
     * @param slaID
     * @param replyTo
     * @param pillarID
     * @param fileIDs
     * @param completeCode
     * @param completeText
     * @param resultingFileIDs
     * @return test message
     */
    public static GetFileIDsComplete getGetFileIDsCompleteTestMessage(
            String correlationID, String slaID, String replyTo, String pillarID, FileIDs fileIDs,
            String completeCode, String completeText, ResultingFileIDs resultingFileIDs) {
        GetFileIDsComplete complete = new GetFileIDsComplete();
        complete.setCorrelationID(correlationID);
        complete.setSlaID(slaID);
        complete.setReplyTo(replyTo);
        complete.setPillarID(pillarID);

        complete.setFileIDs(fileIDs);

        CompleteInfo completeInfo = new CompleteInfo();
        completeInfo.setCompleteCode(completeCode);
        completeInfo.setCompleteText(completeText);
        complete.setCompleteInfo(completeInfo);

        complete.setResultingFileIDs(resultingFileIDs);

        complete.setVersion(VERSION_DEFAULT);
        complete.setMinVersion(VERSION_DEFAULT);
        return complete;
    }
}
