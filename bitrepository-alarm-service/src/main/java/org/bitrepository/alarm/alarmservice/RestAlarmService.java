package org.bitrepository.alarm.alarmservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.bitrepository.alarm.AlarmStore;
import org.bitrepository.alarm.AlarmStoreFactory;


@Path("/AlarmService")
public class RestAlarmService {
    private AlarmStore alarmStore;
    
    public RestAlarmService() {
        alarmStore = AlarmStoreFactory.getInstance();
    }
    
    /**
     * getShortAlarmList exposes the possibility of getting the list of the most recent alarms  
     * @return A string containing the data of the last alarms. 
     */
    @GET
    @Path("/getShortAlarmList/")
    @Produces("text/html")
    public String getShortAlarmList() {
        return alarmStore.getShortList();       
    }
    
    /**
     * getFullAlarmList exposes the possibility of getting the list of all alarms received   
     * @return A string containing the data of all alarms received. 
     */
    @GET
    @Path("/getFullAlarmList/")
    @Produces("text/html")
    public String getFullAlarmList() {
        return alarmStore.getFullList();     
    }
}