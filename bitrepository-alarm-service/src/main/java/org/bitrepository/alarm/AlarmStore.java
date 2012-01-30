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
