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
package org.bitrepository.access.audittrails.client;

import org.bitrepository.protocol.client.BitrepositoryClient;
import org.bitrepository.protocol.eventhandler.EventHandler;

/**
 * Provides functionality for retrieving the list of available audit trail contributors at a given time.
 */
public interface AuditTrailIdentificator extends BitrepositoryClient {
    /**
     * Returns the contributors currently available. A identify contributors request is used to lookup the contributors 
     * each time this method is called.
     */
    void getAvailableContributors(EventHandler eventHandler, String auditTrailInformation);
}
