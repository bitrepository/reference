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

import org.bitrepository.access.getaudittrails.client.AuditTrailClient;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.jaccept.TestEventManager;

public class AuditTrailClientTestWrapper implements AuditTrailClient {
    private AuditTrailClient auditTrailClient;
    private TestEventManager testEventManager;


    public AuditTrailClientTestWrapper(AuditTrailClient auditTrailClient,
                                    TestEventManager testEventManager) {
        this.auditTrailClient = auditTrailClient;
        this.testEventManager = testEventManager;
    }
    @Override
    public void getAuditTrails(AuditTrailQuery[] componentQueries, String fileID, String urlForResult, EventHandler eventHandler,
                               String auditTrailInformation) {
        testEventManager.addStimuli(
                "Calling getAuditTrails(" + componentQueries + ", " + fileID + ", " + urlForResult + ")");
        auditTrailClient.getAuditTrails(componentQueries, fileID, urlForResult, eventHandler, auditTrailInformation);
    }

    @Override
    public void shutdown() {
        // Nothing to do
    }
}
