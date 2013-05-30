/*
 * #%L
 * Bitrepository Integrity Service
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
import org.bitrepository.integrityservice.cache.CollectionStat;
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
        List<CollectionStat> stats = service.getCollectionStatisticsHistory(collectionID, 10);
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
        return service.getCurrentPillarsDataSize();
    }
    
    @GET
    @Path("/getLatestcollectionDataSize/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StatisticsCollectionSize> getLatestCollectionDataSize() {
        List<StatisticsCollectionSize> data = new ArrayList<StatisticsCollectionSize>();
        List<CollectionStat> stats = service.getLatestCollectionStatistics();
        for(CollectionStat stat : stats) {
            StatisticsCollectionSize obj = new StatisticsCollectionSize();
            obj.setCollectionID(stat.getCollectionID());
            obj.setDataSize(stat.getDataSize());
            data.add(obj);
        }
        return data;    
    }
}
