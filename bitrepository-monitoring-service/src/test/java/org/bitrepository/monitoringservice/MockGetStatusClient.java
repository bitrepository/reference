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

import org.bitrepository.access.getstatus.GetStatusClient;
import org.bitrepository.client.eventhandler.EventHandler;

public class MockGetStatusClient implements GetStatusClient {
    private int callsToShutdown = 0;
    @Override
    public void shutdown() {
        callsToShutdown++;
    }
    public int getCallsToShutdown() {
        return callsToShutdown;
    }

    private int callsToGetStatus = 0;
    @Override
    public void getStatus(EventHandler eventHandler) {
        callsToGetStatus++;
    }
    public int getCallsToGetStatus() {
        return callsToGetStatus;
    }
}
