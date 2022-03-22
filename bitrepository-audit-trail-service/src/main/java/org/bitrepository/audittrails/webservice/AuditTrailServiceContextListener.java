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
package org.bitrepository.audittrails.webservice;

import org.bitrepository.audittrails.AuditTrailServiceFactory;
import org.bitrepository.service.AbstractBitrepositoryContextListener;
import org.bitrepository.service.LifeCycledService;

/**
 * The Context listener for the AuditTrailService
 */
public class AuditTrailServiceContextListener extends AbstractBitrepositoryContextListener {

    @Override
    public LifeCycledService getService() {
        return AuditTrailServiceFactory.getAuditTrailService();
    }

    @Override
    public void initialize(String configurationDir) {
        AuditTrailServiceFactory.init(configurationDir);
    }

    @Override
    public String getSettingsParameter() {
        return "auditTrailServiceConfDir";
    }
}
