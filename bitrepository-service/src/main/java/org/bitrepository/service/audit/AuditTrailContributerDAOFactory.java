/*
 * #%L
 * Bitrepository Service
 * %%
 * Copyright (C) 2010 - 2015 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.service.audit;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.database.DatabaseFactory;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * A DatabaseFactory which can return Derby and Postgres AuditTrailContributerDAO's.
 *
 * FIXME: Code does not reflect generic type.
 */
public class AuditTrailContributerDAOFactory extends DatabaseFactory<AuditTrailContributerDAO> {

    public AuditTrailContributerDAO getAuditTrailContributorDAO(DatabaseSpecifics ds, String componentID, Settings settings) {
        AuditTrailContributerDAO dao = getDAOInstance(ds, settings);
        dao.initialize(componentID);
        return dao;
    }
    
    @Override
    protected AuditTrailContributerDAO getDerbyDAO(DatabaseManager dm, Settings settings) {
        return new DerbyAuditTrailContributorDAO(dm);
    }

    @Override
    protected AuditTrailContributerDAO getPostgresDAO(DatabaseManager dm, Settings settings) {
        return new PostgresAuditTrailContributorDAO(dm);
    }

    @Override
    protected DatabaseManager getDatabaseManager(DatabaseSpecifics ds) {
        return new AuditDatabaseManager(ds);
    }
    
}
