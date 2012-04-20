package org.bitrepository.monitoringservice;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * Class for the monitoring service keep a watch on non responding components, and send alarms if needed.
 */
public class MonitoringServiceAlerter {

	private final MessageSender messageSender;
	private final Settings settings;
	private final ComponentStatusStore statusStore;
	private final BigInteger maxRetries;	
	
	public MonitoringServiceAlerter(Settings settings, MessageSender messageSender, ComponentStatusStore statusStore) {
		this.settings = settings;
		this.messageSender = messageSender;
		this.statusStore = statusStore;
		maxRetries = settings.getReferenceSettings().getMonitoringServiceSettings().getMaxRetries();
	}
	
	/**
	 * Check for components that have not responded withing the given constraints, and send alarm
	 * message if there is any. 
	 */
	public void checkStatuses() {
	    Map<String, ComponentStatus> statusMap = statusStore.getStatusMap();
	    List<String> nonRespondingComponents = new ArrayList<String>();
	    for(String ID : statusMap.keySet()) {
	        ComponentStatus componentStatus = statusMap.get(ID);
	        if(componentStatus.getNumberOfMissingReplys() == maxRetries.intValue()) {
	            nonRespondingComponents.add(ID);
	            componentStatus.markAsUnresponsive();
	        }	        
	    }
	    
		if(!nonRespondingComponents.isEmpty()) {
			sendAlarm(nonRespondingComponents);
		}
	}
	
	private void sendAlarm(List<String> components) {
	    AlarmMessage msg = new AlarmMessage();
	    msg.setCollectionID(settings.getCollectionID());
	    msg.setFrom(settings.getReferenceSettings().getMonitoringServiceSettings().getID());
	    msg.setTo(settings.getAlarmDestination());
	    msg.setReplyTo(settings.getCollectionDestination()); //FIXME Probably not right...
	    msg.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
	    msg.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
	    msg.setCorrelationID(UUID.randomUUID().toString());
	    Alarm alarm = new Alarm();
	    alarm.setOrigDateTime(CalendarUtils.getNow());
	    alarm.setAlarmRaiser(settings.getReferenceSettings().getMonitoringServiceSettings().getID());
	    alarm.setAlarmCode(AlarmCode.COMPONENT_FAILURE);
	    alarm.setAlarmText("The following components has become unresponsive: " + components.toString());
	    messageSender.sendMessage(msg);
	}
	
}
