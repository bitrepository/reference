/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.alerter;

/**
 * The integrity alerter, for creating alarms based on an integrity report.
 */
public interface IntegrityAlerter { 
    /**
     * Sends an alarm based on an integrity reporter.
     * @param summary A textual summary of the integrity issues.
     */
    void integrityFailed(String summary, String collectionID);
    
    /**
     * Send an alarm based on an exception.
     * @param issue The reason for the exception.
     * @param collectionID The ID of the collection that the alarm belongs to, may be null
     */
    void operationFailed(String issue, String collectionID);
    
    /**
     * Send an alarm due to a failure in the service
     * @param summary The description of the failure
     * @param collectionID The collection which the failure regards 
     */
    void integrityComponentFailure(String summary, String collectionID); 
}
