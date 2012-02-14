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

/**
 * Implementation of the AlarmHandler interface. 
 * AlarmCollector holds the most recent alarms in memory and persists alarms
 * so nothing should be lost after a restart of the service.  
 */
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
        	synchronized (alarmStoreFile) {
				out = new BufferedWriter(new FileWriter(alarmStoreFile, true));
	            out.write(item.serialize() + "\n");
	            out.flush();
        	}
            addAlarmItemToShortList(item);
        } catch (IOException e) {
        	log.debug(e.getMessage());
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
				log.debug(e.getMessage());
			}
        }
	}
	
	/**
	 * Method to populate the shortAlarmList with alarms from file.. 
	 */
	private void populateShortAlarmList() {
		synchronized (alarmStoreFile) {
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
	            log.debug("Unable find alarm file... '" + file.getAbsolutePath() + "'");
	        } catch (IOException e) {
	            log.debug("Unable to read alarm file... '" + file.getAbsolutePath() + "'");
	        }
		}
	}

}
