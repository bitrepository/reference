/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.common;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.ContributorContext;
import org.bitrepository.service.contributor.ResponseDispatcher;

/**
 * Container for the context of the pillar, e.g. all the components needed for the message handling.
 */
public class MessageHandlerContext extends ContributorContext {
    private final AuditTrailManager auditTrailManager;
    private final String[] pillarCollections;

    /**
     * Delegates to the ContributorContext constructor.
     */
    public MessageHandlerContext(Settings settings,
                                 String[] pillarCollections,
                                 ResponseDispatcher responseDispatcher,
                                 PillarAlarmDispatcher alarmDispatcher,
                                 AuditTrailManager auditTrailManager,
                                 FileExchange fileExchange) {
        super(responseDispatcher, alarmDispatcher, settings, fileExchange);
        ArgumentValidator.checkNotNull(auditTrailManager, "AuditTrailManager");
        this.auditTrailManager = auditTrailManager;
        this.pillarCollections = pillarCollections;
    }

    /**
     * @return The audit trail manger for this context.
     */
    public AuditTrailManager getAuditTrailManager() {
        return auditTrailManager;
    }

    public String[] getPillarCollections() {
        return pillarCollections;
    }

}
