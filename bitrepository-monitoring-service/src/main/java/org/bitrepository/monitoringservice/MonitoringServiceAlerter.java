package org.bitrepository.monitoringservice;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * Class for the monitoring service keep a watch on non responding components, and send alarms if needed.
 */
public class MonitoringServiceAlerter {

	private final MessageSender messageSender;
	private final Settings settings;
	private final BigInteger maxRetries;	
	private final Map<String, Integer> expectedContributors;
	
	public MonitoringServiceAlerter(Settings settings, MessageSender messageSender) {
		this.settings = settings;
		this.messageSender = messageSender;
		expectedContributors = new HashMap<String, Integer>(
				settings.getCollectionSettings().getGetStatusSettings().getContributorIDs().size());
		for(String ID : settings.getCollectionSettings().getGetStatusSettings().getContributorIDs()) {
			expectedContributors.put(ID, 0);
		}
		maxRetries = settings.getReferenceSettings().getMonitoringServiceSettings().getMaxRetries();
	}
	
	/**
	 *  Method to reload counters when status is requested.
	 */
	public void reload() {
		for(String ID : expectedContributors.keySet()) {
			Integer i = expectedContributors.get(ID);
			i++; 
			expectedContributors.put(ID, i);
		}
	}
	
	/**
	 * A component has responded, reset it's not responding count. 
	 */
	public void checkInComponent(String componentID) {
		if(!expectedContributors.containsKey(componentID)) {
			//log unexpected contributor
			return ;
		} else {
			expectedContributors.put(componentID, 0);
		}
	}
	
	/**
	 * Check for components that have not responded withing the given constraints, and send alarm
	 * message if there is any. 
	 */
	public void checkNonRespondingAndSendAlarm() {
		List<String> nonRespondingComponents = new ArrayList<String>();
		for(String ID : expectedContributors.keySet()) {
			if(expectedContributors.get(ID) == maxRetries.intValue()) {
				nonRespondingComponents.add(ID);
			}
		}
		if(!nonRespondingComponents.isEmpty()) {
			sendAlarm(nonRespondingComponents);
		}
	}
	
	private void sendAlarm(List<String> components) {
		//Erhm, send alarm...
	}
	
}
