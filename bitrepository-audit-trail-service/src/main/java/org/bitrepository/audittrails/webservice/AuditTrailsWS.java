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
