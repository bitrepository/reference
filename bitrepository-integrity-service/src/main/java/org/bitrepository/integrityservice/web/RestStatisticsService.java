package org.bitrepository.integrityservice.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.common.webobjects.StatisticsCollectionSize;
import org.bitrepository.common.webobjects.StatisticsDataSize;
import org.bitrepository.common.webobjects.StatisticsPillarSize;
import org.bitrepository.integrityservice.IntegrityService;
import org.bitrepository.integrityservice.IntegrityServiceFactory;
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
    public List<StatisticsDataSize> getDataSizeHistory(@QueryParam("collectionID") String collectionID) {
        List<Date> mockDates = new ArrayList<Date>();
        long startTime = 1360886243;
        mockDates.add(new Date(startTime));
        startTime += 12345;
        mockDates.add(new Date(startTime));
        startTime += 12345;
        mockDates.add(new Date(startTime));
        startTime += 12345;
        mockDates.add(new Date(startTime));
        startTime += 12345;
        mockDates.add(new Date(startTime));
        startTime += 12345;
        mockDates.add(new Date(startTime));
        startTime += 12345;
        mockDates.add(new Date(startTime));
               
        List<StatisticsDataSize> mockData = new ArrayList<StatisticsDataSize>();
        
        Long size =  140000L;
        for(Date date : mockDates) {
            StatisticsDataSize obj = new StatisticsDataSize();
            obj.setDate(date);
            obj.setDateString(TimeUtils.shortDate(date));
            size = (long) (size * 1.23 + 24);
            obj.setDataSize(size);
            mockData.add(obj);
        }
        
        return mockData;
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
