package org.bitrepository.integrityservice.web;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.bitrepository.common.webobjects.StatisticsCollectionSize;
import org.bitrepository.common.webobjects.StatisticsPillarSize;
import org.bitrepository.integrityservice.IntegrityService;
import org.bitrepository.integrityservice.IntegrityServiceFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/Statistics")
public class RestStatisticsService {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private IntegrityService service;

    public RestStatisticsService() {
        this.service = IntegrityServiceFactory.getIntegrityService();
    }
     
    @GET
    @Path("/getDataSizeHistory/")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDataSizeHistory(@QueryParam("collectionID") String collectionID) {
        List<String> mockDates = new ArrayList<String>();
        mockDates.add("14.03.13 12:02");
        mockDates.add("22.03.13 12:05");
        mockDates.add("31.03.13 12:59");
        mockDates.add("11.04.13 03:14");
        mockDates.add("18.04.13 11:31");
        mockDates.add("20.04.13 11:02");
        mockDates.add("24.04.13 12:02");
        JSONArray array = new JSONArray();
        try {
            Long size =  140000L;
            for(String date : mockDates) {
                JSONObject obj = new JSONObject();
                obj.put("date", date);
                size = (long) (size * 1.23 + 24);
                obj.put("dataSize", size);
                array.put(obj);
            }
        } catch (JSONException e) {
            log.debug(e.getMessage());
        }
        
        return array.toString();
    }
    
    @GET
    @Path("/getLatestPillarDataSize/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StatisticsPillarSize> getLatestPillarDataSize() {
        List<String> mockPillarIDs = new ArrayList<String>();
        mockPillarIDs.add("pillarA");
        mockPillarIDs.add("pillarB");
        mockPillarIDs.add("pillarC");
        mockPillarIDs.add("pillar35");
        
        List<StatisticsPillarSize> mockData = new ArrayList<StatisticsPillarSize>();
        
        Long size =  145000L;
        for(String pillar : mockPillarIDs) {
            StatisticsPillarSize obj = new StatisticsPillarSize();
            obj.setPillarID(pillar);
            size = (long) (size * 1.23 + 24);
            obj.setDataSize(size);
            mockData.add(obj);
        }
        
        return mockData;
    }
    
    @GET
    @Path("/getLatestcollectionDataSize/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StatisticsCollectionSize> getLatestCollectionDataSize() {
        List<String> mockCollectionIDs = new ArrayList<String>();
        mockCollectionIDs.add("collectionA");
        mockCollectionIDs.add("collectionB");
        mockCollectionIDs.add("collectionC");
        mockCollectionIDs.add("collection23");
        
        List<StatisticsCollectionSize> mockData = new ArrayList<StatisticsCollectionSize>();
        Long size =  154000L;
        for(String collection : mockCollectionIDs) {
            StatisticsCollectionSize obj = new StatisticsCollectionSize();
            obj.setCollectionID(collection);
            size = (long) (size * 1.23 + 24);
            obj.setDataSize(size);
            mockData.add(obj);
        }
        
        return mockData;    
    }
}
