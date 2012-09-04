/*
 * #%L
 * Bitrepository Access
 * 
 * $Id$
 * $HeadURL$
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

import org.bitrepository.client.BitrepositoryClient;
import org.bitrepository.client.eventhandler.EventHandler;

public interface AuditTrailClient extends BitrepositoryClient {
    /**
     * @param componentQueries Defines which components to retrieve audit trail from. Also defines a filter which
     *                         can be used to limit the audit trail result from each pillar. If null all audit trails
     *                         from all contributers are returned.
     * @param fileID The optional fileID to retrieve audit trails for. If <code>null</code> audit trail are retrieved for
     *               all files.
     * @param urlForResult If defined, the result is upload to this url (with a -componentID postfix) in stead of being
     *                     returned in a completeEvent.
     * @param eventHandler The handler which should receive notifications of the progress events.
     * @param auditTrailInformation The audit information for the given operation. E.g. who is behind the operation call.
     */
    void getAuditTrails(
            AuditTrailQuery[] componentQueries,
            String fileID,
            String urlForResult,
            EventHandler eventHandler, String auditTrailInformation);
}
