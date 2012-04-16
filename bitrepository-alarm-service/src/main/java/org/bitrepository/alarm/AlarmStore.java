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
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.protocol.service.BitrepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmStore implements BitrepositoryService {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	private AlarmService alarmService;
	private String alarmStoreFile;
	private AlarmCollector collector;
	private AlarmMailer mailer;
	private ArrayBlockingQueue<AlarmStoreDataItem> shortAlarmList;
    private MessageAuthenticator authenticator;
    private MessageSigner signer;
    private OperationAuthorizor authorizer;
    private PermissionStore permissionStore;
    private SecurityManager securityManager;

	AlarmStore(Settings settings, String alarmStoreFile, String privateKeyFile) {
		this.alarmStoreFile = alarmStoreFile;
		shortAlarmList = new ArrayBlockingQueue<AlarmStoreDataItem>(20);
		permissionStore = new PermissionStore();
        authenticator = new BasicMessageAuthenticator(permissionStore);
        signer = new BasicMessageSigner();
        authorizer = new BasicOperationAuthorizor(permissionStore);
        securityManager = new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile, 
                authenticator, signer, authorizer, permissionStore, 
                settings.getReferenceSettings().getAlarmServiceSettings().getID());
		alarmService = AlarmComponentFactory.getInstance().getAlarmService(settings, securityManager);
		collector = new AlarmCollector(this.alarmStoreFile, shortAlarmList);
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
		// TODO BITMAG-425
		return "<p>Delivery of full list is not yet implemented..</p>";
	}
}
