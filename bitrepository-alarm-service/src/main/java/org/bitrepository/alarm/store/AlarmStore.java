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
package org.bitrepository.alarm.store;

import java.util.Collection;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;

public interface AlarmStore {
    /**
     * Add an alarm to the store.
     * @param alarm The alarm to be added to the store.
     */
    void addAlarm(Alarm alarm);
    
    /**
     * Extracts the alarms based on the given optional restictions.
     * @param componentID [OPTIONAL] The id of the component.
     * @param alarmCode [OPTIONAL] The alarm code.
     * @param minDate [OPTIONAL] The earliest date for the alarms.
     * @param maxDate [OPTIONAL] The latest date for the alarms.
     * @param fileID [OPTIONAL] The id of the file, which the alarms are connected.
     * @param count [OPTIONAL] The maximum number of alarms to retrieve from the store.
     * @return The requested collection of alarms from the store.
     */
    Collection<Alarm> extractAlarms(String componentID, AlarmCode alarmCode, Date minDate, Date maxDate, 
            String fileID, Integer count);
//    private Logger log = LoggerFactory.getLogger(this.getClass());
//    private AlarmService alarmService;
//    private String alarmStoreFile;
//    private AlarmCollector collector;
//    private AlarmMailer mailer;
//    private ArrayBlockingQueue<AlarmStoreDataItem> shortAlarmList;
//    private MessageAuthenticator authenticator;
//    private MessageSigner signer;
//    private OperationAuthorizor authorizer;
//    private PermissionStore permissionStore;
//    private SecurityManager securityManager;
//    
//    AlarmStore(Settings settings, String alarmStoreFile, String privateKeyFile) {
//        this.alarmStoreFile = alarmStoreFile;
//        shortAlarmList = new ArrayBlockingQueue<AlarmStoreDataItem>(20);
//        permissionStore = new PermissionStore();
//        authenticator = new BasicMessageAuthenticator(permissionStore);
//        signer = new BasicMessageSigner();
//        authorizer = new BasicOperationAuthorizor(permissionStore);
//        securityManager = new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile, 
//                authenticator, signer, authorizer, permissionStore, 
//                settings.getReferenceSettings().getAlarmServiceSettings().getID());
//        alarmService = AlarmComponentFactory.getInstance().getAlarmService(settings, securityManager);
//        collector = new AlarmCollector(this.alarmStoreFile, shortAlarmList);
//        alarmService.addHandler(collector, settings.getAlarmDestination()); 
//        if(settings.getReferenceSettings().getAlarmServiceSettings().getMailingConfiguration() != null) {
//            mailer = new AlarmMailer(settings.getReferenceSettings().getAlarmServiceSettings());
//            alarmService.addHandler(mailer, settings.getAlarmDestination());
//            log.info("ReferenceSettings contained mailer configuration, alarm mailer added.");
//        } else {
//            log.info("ReferenceSettings contained no mailer configuration, no alarm mailer added.");
//        }
//        
//    }
//    public void start() {
//        //Nothing to do
//    }
//    
//    public void shutdown() {
//        alarmService.shutdown();
//    }
//    
//    public ArrayBlockingQueue<AlarmStoreDataItem> getShortList() {
//        return shortAlarmList;
//    }
//    
//    public String getFullList() {
//        // TODO BITMAG-425
//        return "<p>Delivery of full list is not yet implemented..</p>";
//    }
}
