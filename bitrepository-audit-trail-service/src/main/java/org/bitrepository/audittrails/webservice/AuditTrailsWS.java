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
package org.bitrepository.audittrails.webservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.bitrepository.audittrails.store.AuditTrailStore;

public class AuditTrailsWS {

    private AuditTrailStore store;

    @Path("/reposervice")
    public class Reposervice {

        /**
         * Get exposes the possibility of uploading a file to the bitrepository collection that the webservice 
         * is configured to use. The three parameters are all mandatory.
         * @param fileID Filename of the file to be put in the bitrepository. 
         * @param fileSize Size of the file en bytes
         * @param url Place where the bitrepository pillars can fetch the file from 
         * @return A string indicating if the request was successfully started or has been rejected. 
         */
        @GET
        @Path("/putfile/")
        @Produces("text/plain")
        public String getAuditTrails() {
            return store.getAuditTrails(null, null, null).toString();       
        }
    }
}
