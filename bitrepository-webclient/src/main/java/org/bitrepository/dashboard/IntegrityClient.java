/*
 * #%L
 * Bitrepository Webclient
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.dashboard;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.bitrepository.common.webobjects.StatisticsCollectionSize;
import org.bitrepository.common.webobjects.StatisticsDataSize;
import org.bitrepository.common.webobjects.StatisticsPillarSize;
import org.bitrepository.webservice.ServiceUrl;
import org.bitrepository.webservice.ServiceUrlFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

//api : http://int-bitmag-02.kb.dk:8080/bitrepository-integrity-service/integrity/application.wadl

//examples
//http://int-bitmag-02.kb.dk:8080/bitrepository-integrity-service/integrity/IntegrityService/getCollectionInformation/?collectionID=integrationtest1
//http://int-bitmag-02.kb.dk:8080/bitrepository-integrity-service/integrity/IntegrityService/getWorkflowSetup/?collectionID=integrationtest1
//http://int-bitmag-02.kb.dk:8080/bitrepository-integrity-service/integrity/IntegrityService/getIntegrityStatus/?collectionID=integrationtest1
public class IntegrityClient {

	private final static Logger log = LoggerFactory.getLogger(IntegrityClient.class);

	public static void main(String [] args){
	 String jsonResponse = "Avis samlingen";

	    Gson converter = getJsonDateBuilder();
        Type type = new TypeToken<String>() {
        }.getType();
        String name = converter.fromJson(jsonResponse, type);
	System.out.println(name);
	}
	
	
	
	public static GetCollectionInformation getCollectionInformation(String collectionID) {
		ServiceUrl su = ServiceUrlFactory.getInstance();
		Client c = Client.create();
		WebResource service = c.resource(su.getWebserverUrl() + su.getIntegrityServiceUrl());
		String jsonResponse = service.path("integrity/IntegrityService/getCollectionInformation/").queryParam("collectionID", collectionID).type("application/json").accept(MediaType.APPLICATION_JSON)
				.get(String.class);

		Gson converter = new Gson();
		java.lang.reflect.Type type = new TypeToken<GetCollectionInformation>() {
		}.getType();
		GetCollectionInformation colInfo = converter.fromJson(jsonResponse, type);
		return colInfo;
	}

	public static ArrayList<GetWorkflowSetup> getWorkflowSetup(String collectionID) {
		ServiceUrl su = ServiceUrlFactory.getInstance();
		Client c = Client.create();
		WebResource service = c.resource(su.getWebserverUrl() + su.getIntegrityServiceUrl());
		String jsonResponse = service.path("integrity/IntegrityService/getWorkflowSetup/").queryParam("collectionID", collectionID).type("application/json").accept(MediaType.APPLICATION_JSON)
				.get(String.class);

		Gson converter = new Gson();
		java.lang.reflect.Type type = new TypeToken<ArrayList<GetWorkflowSetup>>() {
		}.getType();
		ArrayList<GetWorkflowSetup> wfList = converter.fromJson(jsonResponse, type);
		return wfList;
	}

	public static ArrayList<GetIntegrityStatus> getIntegrityStatus(String collectionID) {
		ServiceUrl su = ServiceUrlFactory.getInstance();
		Client c = Client.create();
		WebResource service = c.resource(su.getWebserverUrl() + su.getIntegrityServiceUrl());
		String jsonResponse = service.path("integrity/IntegrityService/getIntegrityStatus/").queryParam("collectionID", collectionID).type("application/json").accept(MediaType.APPLICATION_JSON)
				.get(String.class);

		Gson converter = new Gson();
		java.lang.reflect.Type type = new TypeToken<ArrayList<GetIntegrityStatus>>() {
		}.getType();
		ArrayList<GetIntegrityStatus> isList = converter.fromJson(jsonResponse, type);
		return isList;
	}

	public static ArrayList<StatisticsCollectionSize> getCollectionDataSize() {
		ServiceUrl su = ServiceUrlFactory.getInstance();
		Client c = Client.create();
		WebResource service = c.resource(su.getWebserverUrl() + su.getIntegrityServiceUrl());
		String jsonResponse = service.path("integrity/Statistics/getLatestcollectionDataSize/").type("application/json").accept(MediaType.APPLICATION_JSON).get(String.class);

		// Log for debug

		// easy convert to Pojo. Reuse objects from Core-module.
		Gson converter = getJsonDateBuilder();
		Type type = new TypeToken<List<StatisticsCollectionSize>>() {
		}.getType();
		ArrayList<StatisticsCollectionSize> list = converter.fromJson(jsonResponse, type);

		return list;

	}

	public static ArrayList<StatisticsPillarSize> getLatestPillarDataSize() {
		ServiceUrl su = ServiceUrlFactory.getInstance();
		Client c = Client.create();
		WebResource service = c.resource(su.getWebserverUrl() + su.getIntegrityServiceUrl());
		String jsonResponse = service.path("integrity/Statistics/getLatestPillarDataSize/").type("application/json").accept(MediaType.APPLICATION_JSON).get(String.class);

		// easy convert to Pojo. Reuse objects from Core-module.
		Gson converter = getJsonDateBuilder();
		Type type = new TypeToken<List<StatisticsPillarSize>>() {
		}.getType();
		ArrayList<StatisticsPillarSize> list = converter.fromJson(jsonResponse, type);

		return list;
	}

	public static ArrayList<StatisticsDataSize> getDataSizeHistory(String id) {
		ServiceUrl su = ServiceUrlFactory.getInstance();
		Client c = Client.create();
		WebResource service = c.resource(su.getWebserverUrl() + su.getIntegrityServiceUrl());
		String jsonResponse = service.path("integrity/Statistics/getDataSizeHistory/").queryParam("collectionID", id).type("application/json").accept(MediaType.APPLICATION_JSON).get(String.class);

		// easy convert to Pojo. Reuse objects from Core-module.
		Gson converter = getJsonDateBuilder();
		Type type = new TypeToken<List<StatisticsDataSize>>() {
		}.getType();
		ArrayList<StatisticsDataSize> list = converter.fromJson(jsonResponse, type);

		return list;
	}

	public static ArrayList<String> getCollectionIds() {
		ServiceUrl su = ServiceUrlFactory.getInstance();
		Client c = Client.create();
		WebResource service = c.resource(su.getWebserverUrl() + su.getWebclientServiceUrl());

		String jsonResponse = service.path("repo/reposervice/getCollectionIDs").type("application/json").accept(MediaType.APPLICATION_JSON).get(String.class);

		Gson converter = getJsonDateBuilder();

		Type type = new TypeToken<ArrayList<String>>() {
		}.getType();
		ArrayList<String> list = converter.fromJson(jsonResponse, type);
		return list;
	}

	public static String getCollectionName(String pillarId) {
		ServiceUrl su = ServiceUrlFactory.getInstance();
		Client c = Client.create();
		WebResource service = c.resource(su.getWebserverUrl() + su.getWebclientServiceUrl());
		String response = service.path("repo/reposervice/getCollectionName").queryParam("collectionID", pillarId).type("application/json").accept(MediaType.APPLICATION_JSON).get(String.class);
        
		/* This is not JSON
		Gson converter = getJsonDateBuilder();
		Type type = new TypeToken<String>() {
		}.getType();
		String name = converter.fromJson(jsonResponse, type);
        return name;
        */
        return response; //This is the name
		
	}

	// handles java.util.date
	private static Gson getJsonDateBuilder() {
		GsonBuilder builder = new GsonBuilder();

		/*
		 * java.util.Date has now been removed from DTO and is a long instead. This is not needed anymore builder.registerTypeAdapter(Date.class, new
		 * JsonDeserializer<Date>() { public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		 * return new Date(json.getAsJsonPrimitive().getAsLong()); } });
		 */

		Gson converter = builder.create();
		return converter;
	}

}
