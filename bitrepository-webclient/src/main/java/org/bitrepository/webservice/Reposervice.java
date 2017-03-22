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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import org.bitrepository.BasicClient;
import org.bitrepository.BasicClientFactory;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.settings.repositorysettings.Collection;
import org.bitrepository.settings.repositorysettings.ProtocolSettings;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The class exposes the REST webservices provided by the Bitrepository-webclient using Jersey. 
 */

@Path("/reposervice")
public class Reposervice {

    private BasicClient client;


    public Reposervice() {
        client = BasicClientFactory.getInstance();
    }
    
    @GET
    @Path("/getCollectionIDs")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getCollectionIDs() {
        return client.getCollectionIDs();
    }
    
    @GET
    @Path("/getCollectionName")
    @Produces(MediaType.APPLICATION_JSON)
    public String getCollectionName(@QueryParam("collectionID") String collectionID) {
        return SettingsUtils.getCollectionName(collectionID, client.getSettings());
    }
    
    @GET
    @Path("/getCollections")
    @Produces(MediaType.APPLICATION_JSON)
    public List<WebCollection> getCollections() {
        return client.getCollectionIDs().stream().map(
                (String collectionID) -> {
                    String collectionName = SettingsUtils.getCollectionName(collectionID, client.getSettings());
                    return new WebCollection(collectionID, collectionName);
                }).collect(Collectors.toList());
    }
    
    @GET
    @Path("/getRepositoryName")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRepositoryName() {
        return SettingsUtils.getRepositoryName(client.getSettings());
    }
    
    @GET
    @Path("/getConfigurationOverview")
    @Produces(MediaType.APPLICATION_JSON)
    public String getConfigurationOverview() throws IOException {
        StringWriter writer = new StringWriter();
        JsonFactory jf = new JsonFactory();
        JsonGenerator jg = jf.createGenerator(writer);
        jg.writeStartObject();
        jg.writeObjectField("repositoryName", SettingsUtils.getRepositoryName(client.getSettings()));
        writeCollectionsArray(jg);
        writeProtocolSettingsObj(jg);
        jg.writeEndObject();
        jg.flush();
        writer.flush();
        return writer.toString();
    }
    
    private void writeCollectionsArray(JsonGenerator jg) throws JsonGenerationException, IOException {
        List<Collection> collections = client.getSettings().getRepositorySettings().getCollections().getCollection();
        jg.writeArrayFieldStart("collections");
        for(Collection c : collections) {
            jg.writeStartObject();
            jg.writeObjectField("collectionID", c.getID());
            jg.writeObjectField("collectionName", SettingsUtils.getCollectionName(c.getID(), client.getSettings()));
            jg.writeArrayFieldStart("pillars");
            List<String> pillars = c.getPillarIDs().getPillarID();
            for(String p : pillars) {
                jg.writeString(p);
            }
            jg.writeEndArray();
            jg.writeEndObject();
        }
        jg.writeEndArray();
    } 
    
    private void writeProtocolSettingsObj(JsonGenerator jg) throws JsonGenerationException, IOException {
        ProtocolSettings protocolSettings = client.getSettings().getRepositorySettings().getProtocolSettings();
        jg.writeObjectFieldStart("protocolSettings");
        jg.writeObjectField("Allowed fileID pattern", protocolSettings.getAllowedFileIDPattern());
        jg.writeObjectField("Default checksum type", protocolSettings.getDefaultChecksumType());
        jg.writeObjectField("Require message authentication", protocolSettings.isRequireMessageAuthentication());
        jg.writeObjectField("Require operation authorization", protocolSettings.isRequireOperationAuthorization());
        jg.writeObjectField("Require checksum for destructive reqests", protocolSettings.isRequireChecksumForDestructiveRequests());
        jg.writeObjectField("Require checksum for new file", protocolSettings.isRequireChecksumForNewFileRequests());
        jg.writeEndObject();
    }
    
    /**
     * getSettingsSummary provides a summary of some important settings of the Bitrepository collection, herein:
     * - The message bus which that is communicated with
     * - The Pillars in the collection
     * - The Bitrepository collection ID
     * @return The current settings formatted as HTML 
     */
    @GET
    @Path("/getSettingsSummary")
    @Produces("text/plain")
    public String getSummarySettings() {
        return client.getSettingsSummary();
    }
}
