package org.bitrepository.alarm;

import java.util.concurrent.ArrayBlockingQueue;

import org.bitrepository.alarm.handler.AlarmCollector;
import org.bitrepository.alarm.handler.AlarmMailer;
import org.bitrepository.common.settings.Settings;

public class AlarmStore {
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
		//mailer = new AlarmMailer(AlarmComponentFactory.getInstance().getConfig());
		alarmService.addHandler(collector, settings.getAlarmDestination()); 
		//alarmService.addHandler(mailer, settings.getAlarmDestination());
	}
	
	public void shutdown() {
		alarmService.shutdown();
	}
	
	public String getShortList() {
		StringBuilder sb = new StringBuilder();
		sb.append("<table id=\"users\" class=\"ui-widget ui-widget-content\">\n");
		sb.append("<thead>\n");
		sb.append("<tr class=\"ui-widget-header\">\n");
		sb.append("<th width=\"70\">Date</th>\n");
		sb.append("<th width=\"80\">Raiser</th>\n");
		sb.append("<th width=\"80\">Alarm code</th>\n");
		sb.append("<th>Description</th>\n");
		sb.append("</tr>\n");
		sb.append("</thead>\n");
		sb.append("<tbody>\n");
		for(AlarmStoreDataItem item : shortAlarmList) {
			sb.append(item.toString());	
			sb.append("\n");
		}
		sb.append("</tbody>\n");
		sb.append("</table>\n");
		return sb.toString();
	}
	
	public String getFullList() {
		return "<p>Delivery of full list is not yet implemented..</p>";
	}
}
