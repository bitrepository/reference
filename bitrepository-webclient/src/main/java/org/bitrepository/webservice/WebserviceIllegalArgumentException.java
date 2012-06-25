package org.bitrepository.webservice;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class WebserviceIllegalArgumentException extends WebApplicationException {

    public WebserviceIllegalArgumentException(String message) {
        super(Response.status(Response.Status.BAD_REQUEST)
                .entity(message)
                .type(MediaType.TEXT_PLAIN)
                .build());
    }
}
