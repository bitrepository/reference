/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.audittrails.collector;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.access.getaudittrails.client.AuditTrailClient;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.common.settings.Settings;
/**
 * Manages the retrieval of of AuditTrails from contributors
 */
public class AuditTrailCollector {
    private final TimerTask auditCollector;
    private final Timer timer;
    
    private final AuditTrailClient client;
    
    private final AuditTrailStore store;
    
    private final Settings settings;
    
    public AuditTrailCollector(Settings settings, AuditTrailClient client, AuditTrailStore store) {
        this.client = client;
        this.settings = settings;
        this.store = store;
        this.timer = new Timer();
        
        auditCollector = new AuditTimerTask();
        timer.schedule(auditCollector, 3600000);
    }
    
    public void collectNewestAudits() {
        timer.purge();
        auditCollector.cancel();
        timer.schedule(auditCollector, 3600000);        
    }
    
    public List<AuditTrailEvent> collectNewAuditTrails() {
        return null;
    }
    
    private class AuditTimerTask extends TimerTask {
        @Override
        public void run() {
            collectNewestAudits();
        }
    }
}
