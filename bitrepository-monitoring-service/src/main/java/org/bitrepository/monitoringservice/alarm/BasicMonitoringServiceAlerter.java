/*
 * #%L
 * Bitrepository Monitoring Service
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
package org.bitrepository.monitoringservice.alarm;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.monitoringservice.status.ComponentStatus;
import org.bitrepository.monitoringservice.status.StatusStore;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.settings.referencesettings.AlarmLevel;

/**
 * Class for the monitoring service keep a watch on non responding components, and send alarms if needed.
 */
public class BasicMonitoringServiceAlerter extends AlarmDispatcher implements MonitorAlerter {
    /** The store for the status results from the components.*/
    private final StatusStore statusStore;
    /** The maximum number of missing replies before an alarm is dispatched.*/
    private final BigInteger maxRetries;
    
    /**
     * @param statusStore The store for the status results from the components.
     * @param sender Used for sending the alarms.
     * @param alarmLevel Only send alarms at this alarms level or higher.
     * @param statusStore Used to maintain the status of the components.
     */
    public BasicMonitoringServiceAlerter(
        Settings settings, MessageSender sender, AlarmLevel alarmLevel, StatusStore statusStore) {
        super(settings, sender, alarmLevel);
        this.statusStore = statusStore;
        maxRetries = settings.getReferenceSettings().getMonitoringServiceSettings().getMaxRetries();
    }
    
    @Override
    public void checkStatuses() {
        Map<String, ComponentStatus> statusMap = statusStore.getStatusMap();
        List<String> nonRespondingComponents = new ArrayList<String>();
        for(String ID : statusMap.keySet()) {
            ComponentStatus componentStatus = statusMap.get(ID);
            if(componentStatus.getNumberOfMissingReplies() >= maxRetries.intValue()) {
                componentStatus.markAsUnresponsive();
                if(!componentStatus.hasAlarmed()) {
                	nonRespondingComponents.add(ID);
                	componentStatus.alarmed();
                }
            }
        }
        
        if(!nonRespondingComponents.isEmpty()) {
            Alarm alarm = new Alarm();
            alarm.setAlarmCode(AlarmCode.COMPONENT_FAILURE);
            alarm.setAlarmText("The following components have become " +
                    "unresponsive: "
                    + nonRespondingComponents.toString());
            error(alarm);
        }
    }
}
