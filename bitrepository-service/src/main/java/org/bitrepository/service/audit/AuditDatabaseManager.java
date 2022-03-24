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

import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.DatabaseMigrator;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * Manager for the audit trail contributor database. For usage, see @DatabaseManager.
 */
public class AuditDatabaseManager extends DatabaseManager {

    private final DatabaseSpecifics databaseSpecifics;
    private DatabaseMigrator migrator;
    public final static String AUDIT_CONTRIBUTOR_DATABASE_SCHEMA = "sql/derby/auditContributorDBCreation.sql";

    public AuditDatabaseManager(DatabaseSpecifics databaseSpecifics) {
        this.databaseSpecifics = databaseSpecifics;
    }

    @Override
    protected DatabaseSpecifics getDatabaseSpecifics() {
        return databaseSpecifics;
    }

    @Override
    protected synchronized DatabaseMigrator getMigrator() {
        if (migrator == null) {
            migrator = new AuditTrailContributorDatabaseMigrator(connector);
        }
        return migrator;
    }

    @Override
    protected boolean needsMigration() {
        return getMigrator().needsMigration();
    }

    @Override
    protected String getDatabaseCreationScript() {
        return AUDIT_CONTRIBUTOR_DATABASE_SCHEMA;
    }

}
