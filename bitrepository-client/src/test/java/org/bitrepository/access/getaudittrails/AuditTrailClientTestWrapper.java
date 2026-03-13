/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getaudittrails;

import io.qameta.allure.Allure;
import org.bitrepository.client.eventhandler.EventHandler;

import java.util.Arrays;
import java.util.Locale;

public class AuditTrailClientTestWrapper implements AuditTrailClient {
    private final AuditTrailClient auditTrailClient;

    public AuditTrailClientTestWrapper(AuditTrailClient auditTrailClient) {
        this.auditTrailClient = auditTrailClient;

    }
    @Override
    public void getAuditTrails(String collectionID, AuditTrailQuery[] componentQueries, String fileID,
                               String urlForResult,
                               EventHandler eventHandler, String auditTrailInformation) {
        if (Allure.getLifecycle().getCurrentTestCase().isPresent()) {
            String stepName = "Calling getAuditTrails for: " + (fileID != null ? fileID : "all files");

            String details =
                    String.format(Locale.ROOT,
                            "Collection: %s%nComponent Queries: %s%nURL for Result: %s%nAudit Info: %s",
                    collectionID, componentQueries == null ? "null" : Arrays.asList(componentQueries),
                    urlForResult, auditTrailInformation);

            Allure.step(stepName, () -> {
                Allure.addAttachment("AuditTrails Request Parameters", details);
                auditTrailClient.getAuditTrails(collectionID, componentQueries, fileID, urlForResult, eventHandler,
                        auditTrailInformation);
            });
        } else {
            auditTrailClient.getAuditTrails(collectionID, componentQueries, fileID, urlForResult, eventHandler,
                    auditTrailInformation);
        }
    }
}
