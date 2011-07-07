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
package org.bitrepository.access.getfileids;

import org.bitrepository.bitrepositoryelements.*;
import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.protocol.TestMessageFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates test messages for use in GetFileIDsClientTest.
 */
public class TestGetFileIDsMessageFactory extends TestMessageFactory {
    protected final String slaID;

    public TestGetFileIDsMessageFactory(String slaID) {
        super();
        this.slaID = slaID;
    }

    /**
     * Generate IdentifyPillarsForGetFileIDsRequest test message with default values.
     * @return test message
     */
    public IdentifyPillarsForGetFileIDsRequest getIdentifyPillarsForGetFileIDsRequestTestMessage() {
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
    public IdentifyPillarsForGetFileIDsRequest getIdentifyPillarsForGetFileIDsRequestTestMessage(
            String correlationID, String slaID, String replyTo, List<String> fileIDlist) {
        IdentifyPillarsForGetFileIDsRequest request = new IdentifyPillarsForGetFileIDsRequest();
        request.setCorrelationID(correlationID);
        request.setBitRepositoryCollectionID(slaID);
        request.setReplyTo(replyTo);
        FileIDs fileIDs = new FileIDs();
        request.setFileIDs(fileIDs);
        request.setVersion(VERSION_DEFAULT);
        request.setMinVersion(VERSION_DEFAULT);
        return request;
    }
    /**
     * Generate IdentifyPillarsForGetFileIDsProgressResponse test message with default values.
     * @param pillarID
     * @return test message
     */
    public IdentifyPillarsForGetFileIDsResponse getIdentifyPillarsForGetFileIDsResponseTestMessage(
            String pillarID) {
        return getIdentifyPillarsForGetFileIDsResponseTestMessage(
                CORRELATION_ID_DEFAULT, SLA_ID_DEFAULT, REPLY_TO_DEFAULT, pillarID, new FileIDs(),
                TIME_MEASURE_UNIT_DEFAULT, TIME_MEASURE_VALUE_DEFAULT);
    }
    /**
     * Generate IdentifyPillarsForGetFileIDsProgressResponse test message with specified values.
     * @param correlationID
     * @param slaID
     * @param replyTo
     * @param pillarID
     * @param fileIDs
     * @param timeMeasureUnit
     * @param timeMeasureValue
     * @return test message
     */
    public IdentifyPillarsForGetFileIDsResponse getIdentifyPillarsForGetFileIDsResponseTestMessage(
            String correlationID, String slaID, String replyTo, String pillarID, FileIDs fileIDs,
            String timeMeasureUnit, BigInteger timeMeasureValue) {
        IdentifyPillarsForGetFileIDsResponse response = new IdentifyPillarsForGetFileIDsResponse();
        response.setCorrelationID(correlationID);
        response.setBitRepositoryCollectionID(slaID);
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

    public GetFileIDsRequest getGetFileIDsRequestTestMessage(String pillarID) {
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
    public GetFileIDsRequest getGetFileIDsRequestTestMessage(
            String correlationID, String slaID, String replyTo, String pillarID, FileIDs fileIDs) {
        GetFileIDsRequest request = new GetFileIDsRequest();
        request.setCorrelationID(correlationID);
        request.setBitRepositoryCollectionID(slaID);
        request.setReplyTo(replyTo);
        request.setPillarID(pillarID);
        request.setFileIDs(fileIDs);

        request.setVersion(VERSION_DEFAULT);
        request.setMinVersion(VERSION_DEFAULT);
        return request;
    }
    /**
     * Generate GetFileIDsProgressResponse test message with default values.
     * @param pillarID
     * @return test message
     */
    public GetFileIDsProgressResponse getGetFileIDsResponseTestMessage(String pillarID) {
        return getGetFileIDsResponseTestMessage(
                CORRELATION_ID_DEFAULT, SLA_ID_DEFAULT, REPLY_TO_DEFAULT, pillarID, new FileIDs(),
                RESPONSE_CODE_DEFAULT, RESPONSE_TEXT_DEFAULT);
    }
    /**
     * Generate GetFileIDsProgressResponse test message with specified values.
     * @param correlationID
     * @param slaID
     * @param replyTo
     * @param pillarID
     * @param fileIDs
     * @param responseCode
     * @param responseText
     * @return test message
     */
    public GetFileIDsProgressResponse getGetFileIDsResponseTestMessage(
            String correlationID, String slaID, String replyTo, String pillarID, FileIDs fileIDs,
            String responseCode, String responseText) {
        GetFileIDsProgressResponse response = new GetFileIDsProgressResponse();
        response.setCorrelationID(correlationID);
        response.setBitRepositoryCollectionID(slaID);
        response.setReplyTo(replyTo);
        response.setPillarID(pillarID);
        response.setFileIDs(fileIDs);

        ProgressResponseInfo responseInfo = new ProgressResponseInfo();
        responseInfo.setProgressResponseCode(responseCode);
        responseInfo.setProgressResponseText(responseText);
        response.setProgressResponseInfo(responseInfo);

        response.setVersion(VERSION_DEFAULT);
        response.setMinVersion(VERSION_DEFAULT);
        return response;
    }

    /**
     * Generate GetFileIDsFinalResponse test message with default values.
     * @param pillarID
     * @return test message
     */
    public GetFileIDsFinalResponse getGetFileIDsCompleteTestMessage(String pillarID) {
        return getGetFileIDsFinalResponseTestMessage(
                CORRELATION_ID_DEFAULT, SLA_ID_DEFAULT, REPLY_TO_DEFAULT, pillarID, new FileIDs(),
                COMPLETE_CODE_DEFAULT, COMPLETE_TEXT_DEFAULT, new ResultingFileIDs());
    }
    /**
     * Generate GetFileIDsFinalResponse test message with specified values.
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
    public GetFileIDsFinalResponse getGetFileIDsFinalResponseTestMessage(
            String correlationID, String slaID, String replyTo, String pillarID, FileIDs fileIDs,
            String completeCode, String completeText, ResultingFileIDs resultingFileIDs) {
        GetFileIDsFinalResponse complete = new GetFileIDsFinalResponse();
        complete.setCorrelationID(correlationID);
        complete.setBitRepositoryCollectionID(slaID);
        complete.setReplyTo(replyTo);
        complete.setPillarID(pillarID);

        complete.setFileIDs(fileIDs);

        FinalResponseInfo completeInfo = new FinalResponseInfo();
        completeInfo.setFinalResponseCode(completeCode);
        completeInfo.setFinalResponseText(completeText);
        complete.setFinalResponseInfo(completeInfo);

        complete.setResultingFileIDs(resultingFileIDs);

        complete.setVersion(VERSION_DEFAULT);
        complete.setMinVersion(VERSION_DEFAULT);
        return complete;
    }
}
