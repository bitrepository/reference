package org.bitrepository.integrityservice.web;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.common.webobjects.StatisticsCollectionSize;
import org.bitrepository.common.webobjects.StatisticsDataSize;
import org.bitrepository.common.webobjects.StatisticsPillarSize;
import org.bitrepository.integrityservice.IntegrityServiceManager;
import org.bitrepository.integrityservice.cache.CollectionStat;
import org.bitrepository.integrityservice.cache.IntegrityModel;

@Path("/Statistics")
public class RestStatisticsService {
    private IntegrityModel model;

    public RestStatisticsService() {
        this.model = IntegrityServiceManager.getIntegrityModel();
    }
     
    @GET
    @Path("/getDataSizeHistory/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StatisticsDataSize> getDataSizeHistory(@QueryParam("collectionID") String collectionID) {
        List<CollectionStat> stats = model.getLatestCollectionStat(collectionID, 10);
        List<StatisticsDataSize> data = new ArrayList<StatisticsDataSize>();
        for(CollectionStat stat : stats) {
            StatisticsDataSize obj = new StatisticsDataSize();
            Date statTime = stat.getStatsTime();
            obj.setDateMillis(statTime.getTime());
            obj.setDateString(TimeUtils.shortDate(statTime));
            obj.setDataSize(stat.getDataSize());
            data.add(obj);
        }
        
        return data;
    }
    
    @GET
    @Path("/getDataSizeHistoryMocked/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StatisticsDataSize> getDataSizeHistoryMock(@QueryParam("collectionID") String collectionID) {
        List<Date> mockDates = new ArrayList<Date>();
        long startTime = 1360886243*1000;
        mockDates.add(new Date(startTime));
        startTime += 12345*1000;
        mockDates.add(new Date(startTime));
        startTime += 12345*1000;
        mockDates.add(new Date(startTime));
        startTime += 12345*1000;
        mockDates.add(new Date(startTime));
        startTime += 12345*1000;
        mockDates.add(new Date(startTime));
        startTime += 12345*1000;
        mockDates.add(new Date(startTime));
        startTime += 12345*1000;
        mockDates.add(new Date(startTime));
               
        List<StatisticsDataSize> mockData = new ArrayList<StatisticsDataSize>();
        
        Long size =  140000L;
        for(Date date : mockDates) {
            StatisticsDataSize obj = new StatisticsDataSize();
            obj.setDateMillis(date.getTime());
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
        return getCurrentPillarsDataSize();
    }
    
    @GET
    @Path("/getLatestcollectionDataSize/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StatisticsCollectionSize> getLatestCollectionDataSize() {
        List<StatisticsCollectionSize> data = new ArrayList<StatisticsCollectionSize>();
        List<CollectionStat> stats = getLatestCollectionStatistics();
        for(CollectionStat stat : stats) {
            StatisticsCollectionSize obj = new StatisticsCollectionSize();
            obj.setCollectionID(stat.getCollectionID());
            obj.setDataSize(stat.getDataSize());
            data.add(obj);
        }
        return data;    
    }

    public List<CollectionStat> getLatestCollectionStatistics() {
        List<CollectionStat> res = new ArrayList<CollectionStat>();
        for(String collection : SettingsUtils.getAllCollectionsIDs()) {
            List<CollectionStat> stats = model.getLatestCollectionStat(collection, 1);
            if(!stats.isEmpty()) {
                res.add(stats.get(0));
            }
        }
        return res;
    }

    public List<StatisticsPillarSize> getCurrentPillarsDataSize() {
        List<StatisticsPillarSize> stats = new ArrayList<StatisticsPillarSize>();
        for(String pillar : SettingsUtils.getAllPillarIDs()) {
            StatisticsPillarSize stat = new StatisticsPillarSize();
            Long dataSize = model.getPillarDataSize(pillar);
            stat.setDataSize(dataSize);
            stat.setPillarID(pillar);
            stats.add(stat);
        }
        return stats;
    }
}
