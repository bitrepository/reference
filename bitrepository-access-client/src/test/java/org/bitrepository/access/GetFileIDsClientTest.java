/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access;

import org.jaccept.structure.ExtendedTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Test class for the 'GetFileIDsClient'.
 * @author kfc
 */
public class GetFileIDsClientTest extends ExtendedTestCase {

    private Logger log = LoggerFactory.getLogger(GetFileIDsClientTest.class);

    private static int WAITING_TIME_FOR_MESSAGE = 1000;

    private static String slaId = "THE-SLA";
    private static String pillarId1 = "pillarId1";
    private static String pillarId2 = "pillarId2";
    private static String pillarId3 = "pillarId3";
    private static String fileId1 = "fileId1";
    private static String fileId2 = "fileId2";
    private static String fileId3 = "fileId3";

    @Test(groups = {"specificationonly"})
    public void identifyPillarsForGetFileIDsTest() throws Exception {
        addDescription("Tests that the expected number of pillars reply to " +
                "request");
        GetFileIDsClient client = new GetFileIDsClientImpl();
        addStep("Add three reference pillars that reply to given SLA",
                "Logging on INFO level that reports the pillars started");
        //Todo Reference Pillar
        addStep("Send a message identifying pillars",
                "Logging of one request message and three reply messages " +
                        "(All pillars should reply (they have no files, but " +
                        "will be able to give the empty list of FileIDs).");
        client.identifyPillarsForGetFileIDs(slaId);
        addStep("The GetFileIDsClient receives the messages.",
                "Logging on INFO level of received messages.");
    }

    @Test(groups = {"specificationonly"})
    public void GetFileIDsTest() throws Exception {
        addDescription("Tests that a pillar returns the expected list of " +
                "FileIDs");
        addStep("Add a reference pillar that replies to given SLA",
                "Logging on INFO level that reports the pillar started");

        addStep("Send a message identifying pillars",
                "Logging of request message + reply message with a ReplyTo queue" +
                        "where the getFileIDsRequest can be send");
        addStep("Send a message to get FileIDs",
                "The returned list of FileIDs should be empty");
        addStep("Put three files with known IDs into the Bit Repository " +
                "under given SLA", "Received PutFileComplete messages with " +
                "positive CompleteCode");
        addStep("Send a message to get FileIDs",
                "The returned list of FileIDs should contain the three known " +
                        "IDs");
    }
}
