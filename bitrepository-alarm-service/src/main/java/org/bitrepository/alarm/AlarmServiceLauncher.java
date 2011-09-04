/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id: AlarmClient.java 239 2011-07-22 13:51:09Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-alarm-client/src/main/java/org/bitrepository/alarm/AlarmClient.java $
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
package org.bitrepository.alarm;

import org.bitrepository.alarm.handler.AlarmLoggingHandler;
import org.bitrepository.alarm_service.alarmconfiguration.AlarmConfiguration;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.configuration.ProtocolConfiguration;
import org.bitrepository.protocol.settings.XMLFileSettingsLoader;

/**
 * Class for launching an alarm service.
 */
public class AlarmServiceLauncher {
    private static final String COLLECTION_ID = "bitrepository-devel";
    private static final String PATH_TO_SETTINGS = "settings/xml";

    /**
     * @param args
     */
    public static void main(String[] args) {
        ProtocolConfiguration pConf = ProtocolComponentFactory.getInstance().getProtocolConfiguration();
        AlarmConfiguration aConf = AlarmComponentFactory.getInstance().getConfig();

        AlarmSettingsLoader settingsLoader = 
            new AlarmSettingsLoader(new XMLFileSettingsLoader(PATH_TO_SETTINGS));

        MutableAlarmSettings settings;
        try {
            settings = settingsLoader.loadSettings(COLLECTION_ID);
            // Instantiate the AlarmService.
            AlarmService alarm = AlarmComponentFactory.getInstance().getAlarmService(settings);
            alarm.addHandler(new AlarmLoggingHandler(), aConf.getQueue());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
