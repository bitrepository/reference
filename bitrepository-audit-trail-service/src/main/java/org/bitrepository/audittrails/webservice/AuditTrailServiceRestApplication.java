package org.bitrepository.audittrails.webservice;


import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import jakarta.ws.rs.core.Application;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AuditTrailServiceRestApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return new HashSet<>(Arrays.asList(JacksonJsonProvider.class, RestAuditTrailService.class));
    }
}
