/*
 * #%L
 * Bitrepository Integration
 * 
 * $Id: AlarmDispatcher.java 627 2011-12-09 15:13:13Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/main/java/org/bitrepository/pillar/messagehandler/AlarmDispatcher.java $
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityservice.alerter;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.checking.reports.IntegrityReporter;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.settings.referencesettings.AlarmLevel;

/**
 * The class for dispatching alarms in the integrity service.
 */
public class IntegrityAlarmDispatcher extends AlarmDispatcher implements IntegrityAlerter {

    /**
     * Delegates to the AlarmDispatcher dispatcher.
     * @param settings The settings to use.
     * @param sender Used for sending the alarms.
     * @param alarmLevel Only send alarms at this alarms level or higher.
     */
    public IntegrityAlarmDispatcher(Settings settings, MessageSender sender, AlarmLevel alarmLevel) {
        super(settings, sender, alarmLevel);
    }

    @Override
    public void operationFailed(String issue, String collectionID) {
        Alarm ad = new Alarm();
        ad.setAlarmCode(AlarmCode.FAILED_OPERATION);
        ad.setAlarmText(issue);
        ad.setCollectionID(collectionID);
        error(ad);
    }

    @Override
    public void integrityFailed(IntegrityReporter reporter) {
        Alarm ad = new Alarm();
        ad.setAlarmCode(AlarmCode.INTEGRITY_ISSUE);
        ad.setAlarmText(reporter.generateSummaryOfReport());
        ad.setCollectionID(reporter.getCollectionID());
        error(ad);        
    }
}