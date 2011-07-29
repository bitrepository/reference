/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.modify.putfile;

import org.bitrepository.protocol.bitrepositorycollection.ClientSettings;
import org.bitrepository.protocol.bitrepositorycollection.MutableClientSettings;

/**
 * The instantiateable PutFileSettings. Can set all the variables in the PutFileClientSettings.
 */
public class MutablePutFileClientSettings extends MutableClientSettings implements PutFileClientSettings {
    /**
     * Constructor.
     * @param settings The client settings.
     */
    public MutablePutFileClientSettings(ClientSettings settings) {
        super(settings);
    }

    /** The settings for the default timeout for the put operation.*/
    private long putFileDefaultTimeout = 0;
    
    
    @Override
    public long getPutFileDefaultTimeout() {
        return putFileDefaultTimeout;
    }
    /**
     * Method for setting the default timeout for the put operation.
     * @param timeout The new timeout for the put file operation.
     */
    public void setPutFileDefaultTimeout(long timeout) {
        putFileDefaultTimeout = timeout;
    }

    /** The information about how to handle the audit-trails.*/
    private String auditTrailInformation = null;
    
    @Override
    public String getAuditTrailInformation() {
        return auditTrailInformation;
    }
    /**
     * Method for setting the information about how to handle the audit-trails.
     * @param auditInfo The new audit-trail information.
     */
    public void setAuditTrailInformation(String auditInfo) {
        auditTrailInformation = auditInfo;
    }
}
