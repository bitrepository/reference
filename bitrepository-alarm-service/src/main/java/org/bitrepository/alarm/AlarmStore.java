/*
 * #%L
 * Bitrepository Alarm Service
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.alarm;

import java.util.concurrent.ArrayBlockingQueue;

import org.bitrepository.alarm.handler.AlarmCollector;
import org.bitrepository.alarm.handler.AlarmMailer;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.settings.referencesettings.MailingConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmStore {
    private Logger log = LoggerFactory.getLogger(this.getClass());
	private AlarmService alarmService;
	private String alarmStoreFile;
	private AlarmCollector collector;
	private AlarmMailer mailer;
    private ArrayBlockingQueue<AlarmStoreDataItem> shortAlarmList;
	
	AlarmStore(Settings settings, String alarmStoreFile) {
		this.alarmStoreFile = alarmStoreFile;
		shortAlarmList = new ArrayBlockingQueue<AlarmStoreDataItem>(20);
		alarmService = AlarmComponentFactory.getInstance().getAlarmService(settings);
		collector = new AlarmCollector(alarmStoreFile, shortAlarmList);
		alarmService.addHandler(collector, settings.getAlarmDestination()); 
		if(settings.getReferenceSettings().getAlarmServiceSettings().getMailingConfiguration() != null) {
			mailer = new AlarmMailer(settings.getReferenceSettings().getAlarmServiceSettings());
			alarmService.addHandler(mailer, settings.getAlarmDestination());
			log.info("ReferenceSettings contained mailer configuration, alarm mailer added.");
		} else {
			log.info("ReferenceSettings contained no mailer configuration, no alarm mailer added.");
		}
		
	}
	
	public void shutdown() {
		alarmService.shutdown();
	}
	
	public ArrayBlockingQueue<AlarmStoreDataItem> getShortList() {
		return shortAlarmList;
	}
	
	public String getFullList() {
		return "<p>Delivery of full list is not yet implemented..</p>";
	}
}
