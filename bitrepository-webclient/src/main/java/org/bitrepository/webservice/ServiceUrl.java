/*
 * #%L
 * Bitrepository Webclient
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
package org.bitrepository.webservice;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/urlservice")
public class ServiceUrl {
    private static final String CONFIGFILE = "services.properties";
    private static final String ALARMURL = "org.bitrepository.webclient.alarmserviceurl"; 
    private static final String AUDITTRAILURL = "org.bitrepository.webclient.audittrailserviceurl"; 
    private static final String INTEGRITYURL = "org.bitrepository.webclient.integrityserviceurl"; 
    private static final String MONITORINGURL = "org.bitrepository.webclient.monitoringserviceurl";
    
    private static String alarmUrl = "";
    private static String auditTrailUrl = "";
    private static String integrityUrl = "";
    private static String monitoringUrl = "";
    
    private static String configDir;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public ServiceUrl() {
        if(configDir == null) {
            log.debug("ServiceUrl constructor called before init!!");
            throw new RuntimeException("No configuration dir has been set!");
        }
        log.debug("Called ServiceUrl constructor, confdir: " + configDir);
        loadProperties();
    }

    private void loadProperties() {
        log.debug("Loading service properties..");
        Properties properties = new Properties();
        try {
            String propertiesFile = configDir + "/" + CONFIGFILE;
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(propertiesFile), StandardCharsets.UTF_8));
            properties.load(reader);

            alarmUrl = properties.getProperty(ALARMURL);
            auditTrailUrl = properties.getProperty(AUDITTRAILURL);
            integrityUrl = properties.getProperty(INTEGRITYURL);
            monitoringUrl = properties.getProperty(MONITORINGURL);
            log.debug("Properties has been loaded:");
            log.debug("alarm:" +alarmUrl);
            log.debug("auditTrail:" +auditTrailUrl);
            log.debug("integrity:" +integrityUrl);
        } catch (IOException e) {
            //will just fail setting keystore stuff and we won't be able to connect over ssl
            // not a big deal..
            log.debug("Caught I/O exception while loading properties", e);
        }
    }

    public synchronized static void init(String confDir) {
        configDir = confDir;
    }

    @GET
    @Path("/alarmService")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAlarmServiceUrl() {
        return alarmUrl;
    }
    
    @GET
    @Path("/auditTrailService")
    @Produces(MediaType.APPLICATION_JSON)
    public String getAuditTrailServiceUrl() {
        return auditTrailUrl;
    }

    @GET
    @Path("/integrityService")
    @Produces(MediaType.APPLICATION_JSON)
    public String getIntegrityServiceUrl() {
        return integrityUrl;
    }

    @GET
    @Path("/monitoringService")
    @Produces(MediaType.APPLICATION_JSON)
    public String getMonitoringServiceUrl() {
        return monitoringUrl;
    }
}
