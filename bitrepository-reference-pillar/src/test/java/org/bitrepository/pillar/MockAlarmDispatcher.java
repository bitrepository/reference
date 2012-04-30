/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.pillar.common.PillarAlarmDispatcher;
import org.bitrepository.service.contributor.ContributorContext;

public class MockAlarmDispatcher extends PillarAlarmDispatcher {

    public MockAlarmDispatcher(ContributorContext context) {
        super(context);
    }

    private int callsForSendAlarm = 0;
    @Override
    public void warning(Alarm alarm) {
        sendAlarm();
    }
    @Override
    public void error(Alarm alarm) {
        sendAlarm();
    }
    @Override
    public void emergency(Alarm alarm) {
        sendAlarm();
    }
    private void sendAlarm() {
        callsForSendAlarm++;
        try {
            this.notifyAll();
        } catch (Exception e) {
            // do nothing
        }
    }
    public int getCallsForSendAlarm() {
        return callsForSendAlarm;
    }
    public void resetCallsForSendAlarm() {
        callsForSendAlarm = 0;
    }
}
