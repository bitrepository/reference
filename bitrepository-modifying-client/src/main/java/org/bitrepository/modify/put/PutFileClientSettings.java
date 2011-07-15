/*
 * #%L
 * Bitrepository Access
 * 
 * $Id: GetFileClientSettings.java 199 2011-06-20 07:03:56Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-access-client/src/main/java/org/bitrepository/access/getfile/GetFileClientSettings.java $
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
package org.bitrepository.modify.put;

import org.bitrepository.protocol.bitrepositorycollection.ClientSettings;

/**
 * Settings for the put client.
 */
public interface PutFileClientSettings extends ClientSettings {
    /**
     * Return the default timeout for waiting for a getFile request to finish. 
     * @return The number is in milliseconds.
     */
    long getPutFileDefaultTimeout();
    
    /**
     * Retrieves the audit trail information.
     * @return A string describing the audit-trail information.
     */
    String getAuditTrailInformation();
}
