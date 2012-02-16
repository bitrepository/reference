/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.audittrails.service;

public class AuditTrailServiceFactory {
    
    private static AuditTrailService auditTrailService;
    private static String configurationDir;
    
    /**
     * Private constructor as the class is meant to be used in a static way.
     */
    private AuditTrailServiceFactory() {
        
    }
    
    
    public static void init(String confDir) {
        configurationDir = confDir;
    }
    
    /**
     * Factory method to retrieve AuditTrailService  
     */
    public synchronized static AuditTrailService getAuditTrailService() {
        if(auditTrailService == null) {
            auditTrailService = new AuditTrailService();
        }
        
        return auditTrailService;
    }
}
