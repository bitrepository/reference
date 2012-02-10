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
package org.bitrepository.alarm.handler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;

import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.alarm.AlarmHandler;
import org.bitrepository.alarm.AlarmStoreDataItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmCollector implements AlarmHandler {

	private static final int MAXALARMENTRIES = 10;
    private Logger log = LoggerFactory.getLogger(this.getClass());
	private String alarmStoreFile;
	private ArrayBlockingQueue<AlarmStoreDataItem> shortAlarmList;
	
	public AlarmCollector(String alarmStoreFile, ArrayBlockingQueue<AlarmStoreDataItem> shortAlarmList) {
		this.alarmStoreFile = alarmStoreFile;
		this.shortAlarmList = shortAlarmList;
		populateShortAlarmList();
	}
	
	@Override
    public void handleAlarm(AlarmMessage msg) {
		AlarmStoreDataItem item = new AlarmStoreDataItem(msg);
		BufferedWriter out;
        try {
            out = new BufferedWriter(new FileWriter(alarmStoreFile, true));
            out.write(item.serialize() + "\n");
            out.flush();
            addAlarmItemToShortList(item);
        } catch (IOException e) {
        }
		
	}

	@Override
	public void handleOther(Object msg) {
		log.info("Got something other than an alarm messge: " + msg);

	}
	
	/**
	 * Add a AlarmStoreDataItem to the short list in memory. 
	 */
	private void addAlarmItemToShortList(AlarmStoreDataItem item) {
        shortAlarmList.offer(item);
        if(shortAlarmList.size() > MAXALARMENTRIES) {
        	try {
				shortAlarmList.take();
			} catch (InterruptedException e) {
			}
        }
	}
	
	/**
	 * Method to populate the shortAlarmList with alarms from log.. 
	 */
	private void populateShortAlarmList() {
        File file = new File(alarmStoreFile);
        try {
            FileReader fr = new FileReader(alarmStoreFile);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
            	try {
            	AlarmStoreDataItem item = AlarmStoreDataItem.deserialize(line);
            	addAlarmItemToShortList(item);
            	} catch (IllegalArgumentException e) {
            		log.debug("Seems to have caught a badly formatted line", e);
            	}
            }
        } catch (FileNotFoundException e) {
            log.debug("Unable find log file... '" + file.getAbsolutePath() + "'");
        } catch (IOException e) {
            log.debug("Unable to read log... '" + file.getAbsolutePath() + "'");
        }
	}

}
