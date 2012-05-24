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
package org.bitrepository.monitoringservice;

import java.util.HashMap;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.monitoringservice.status.ComponentStatus;
import org.bitrepository.monitoringservice.status.StatusStore;

public class MockStatusStore implements StatusStore {

    private int callsForUpdateStatus = 0;
    @Override
    public void updateStatus(String componentID, ResultingStatus status) {
        callsForUpdateStatus++;
    }
    public int getCallsForUpdateStatus() {
        return callsForUpdateStatus;
    }

    private int callsForUpdateReplayCounts = 0;
    @Override
    public void updateReplyCounts() {
        callsForUpdateReplayCounts++;
    }
    public int getCallsForUpdateReplayCounts() {
        return callsForUpdateReplayCounts;
    }

    private int callsForGetStatusMap = 0;
    @Override
    public Map<String, ComponentStatus> getStatusMap() {
        callsForGetStatusMap++;
        return new HashMap<String, ComponentStatus>();
    }
    public int getCallsForGetStatusMap() {
        return callsForGetStatusMap;
    }
    
}
