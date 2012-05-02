package org.bitrepository.alarm.store;

import java.util.Collection;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;

public class AlarmDAO implements AlarmStore {
    
    
    
    @Override
    public void addAlarm(Alarm alarm) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public Collection<Alarm> extractAlarms(String componentID, AlarmCode alarmCode, Date minDate, Date maxDate,
            String fileID) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
