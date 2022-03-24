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

import org.bitrepository.client.BitRepositoryClient;
import org.bitrepository.client.eventhandler.EventHandler;

public interface AuditTrailClient extends BitRepositoryClient {
    /**
     * @param collectionID          The collection to request auditTrails for.
     * @param componentQueries      Defines which components to retrieve auditTrail from. Also defines a filter which
     *                              can be used to limit the auditTrail result from each pillar. If <code>null</code> all auditTrails
     *                              from all contributors are returned.
     * @param fileID                The optional fileID to retrieve audit trails for. If <code>null</code> auditTrails are retrieved for
     *                              all files.
     * @param urlForResult          If defined, the result is upload to the defined url (with a -componentID postfix) instead of being
     *                              returned in a completeEvent.
     * @param eventHandler          The handler which should receive notifications of the progress events.
     * @param auditTrailInformation The audit information for the given operation. E.g. who is behind the operation call.
     */
    void getAuditTrails(
            String collectionID,
            AuditTrailQuery[] componentQueries,
            String fileID,
            String urlForResult,
            EventHandler eventHandler, String auditTrailInformation);
}
