package org.bitrepository.integrityclient.web;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/IntegrityService")
public class RestIntegrityService {

    @GET
    @Path("/hello/")
    @Produces("text/html")
    public String getFullAlarmList() {
        return "<h1>Hello World!</h1>";     
    }
}
