/*
 * #%L
 * Bitrepository Access Client
 * 
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
package org.bitrepository.access.getfile;

import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.client.eventhandler.EventHandler;


import java.net.URL;

/**
 * Wraps the <code>GetFileClient</code> adding test event logging and functionality for handling blocking calls.
 */
public class GetFileClientTestWrapper implements GetFileClient {
    private GetFileClient createGetFileClient;

    public GetFileClientTestWrapper(GetFileClient createGetFileClient) {
        this.createGetFileClient = createGetFileClient;
        
    }

    @Override
    public void getFileFromFastestPillar(String collectionID, String fileID, FilePart filePart, URL uploadUrl,
                                         EventHandler eventHandler,
                                         String auditTrailInformation) {
        String stepName = "Calling getFileFromFastestPillar for: " + fileID;
        String details = "Collection: " + collectionID + "\n"
                + "FilePart: " + filePart + "\n"
                + "Upload URL: " + uploadUrl + "\n"
                + "Audit Info: " + auditTrailInformation;

        io.qameta.allure.Allure.step(stepName, () -> {
            io.qameta.allure.Allure.addAttachment("Request Parameters", details);
            createGetFileClient.getFileFromFastestPillar(collectionID,
                    fileID, filePart, uploadUrl, eventHandler, auditTrailInformation);
        });
    }

    @Override
    public void getFileFromSpecificPillar(String collectionID, String fileID, FilePart filePart, URL uploadUrl,
                                          String pillarID,
                                          EventHandler eventHandler, String auditTrailInformation) {
        String stepName = "Calling getFileFromSpecificPillar for: " + fileID + " on " + pillarID;
        String details = "Collection: " + collectionID + "\n"
                + "FilePart: " + filePart + "\n"
                + "Upload URL: " + uploadUrl + "\n"
                + "Pillar: " + pillarID + "\n"
                + "Audit Info: " + auditTrailInformation;

        io.qameta.allure.Allure.step(stepName, () -> {
            io.qameta.allure.Allure.addAttachment("Request Parameters", details);
            createGetFileClient.getFileFromSpecificPillar(collectionID,
                    fileID, filePart, uploadUrl, pillarID, eventHandler, auditTrailInformation);
        });
    }
}
