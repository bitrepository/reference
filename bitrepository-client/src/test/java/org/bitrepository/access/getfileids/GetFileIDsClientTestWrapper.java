/*
 * #%L
 * Bitrepository Access Client
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

import io.qameta.allure.Allure;
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.client.eventhandler.EventHandler;

import java.net.URL;
import java.util.Arrays;

/**
 * Wraps the <code>GetFileIDsClient</code> adding test event logging and functionality.
 */
public class GetFileIDsClientTestWrapper implements GetFileIDsClient {
    /** The actual GetFileIDsClient to perform the operations.*/
    private final GetFileIDsClient client;

    /**
     * Constructor.
     * @param client The actual GetFileIDsClient.
     */
    public GetFileIDsClientTestWrapper(GetFileIDsClient client) {
        this.client = client;
    }

    @Override
    public void getFileIDs(String collectionID, ContributorQuery[] contributorQueries, String fileID,
                           URL addressForResult, EventHandler eventHandler) {
        String stepName = "Calling getFileIDs for: " + (fileID != null ? fileID : "all files");

        StringBuilder details = new StringBuilder();
        details.append("Collection: ").append(collectionID).append("\n")
                .append("Contributor Queries: ").append(contributorQueries == null ? "null" : Arrays.asList(contributorQueries)).append("\n")
                .append("Address for Result: ").append(addressForResult).append("\n")
                .append("EventHandler: ").append(eventHandler);

        Allure.step(stepName, () -> {
            Allure.addAttachment("GetFileIDs Request Parameters", details.toString());
            client.getFileIDs(collectionID, contributorQueries, fileID, addressForResult, eventHandler);
        });
    }
}
