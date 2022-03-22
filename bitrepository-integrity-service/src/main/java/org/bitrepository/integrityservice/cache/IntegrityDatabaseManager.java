/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.cache;

import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.DatabaseMigrator;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * Manager for the database of the IntegrityService. For usage, see @DatabaseManager.
 */
public class IntegrityDatabaseManager extends DatabaseManager {
    private static final String INTEGRITY_SERVICE_DATABASE_SCHEMA = "sql/derby/integrityDBCreation.sql";
    private final DatabaseSpecifics databaseSpecifics;
    private DatabaseMigrator migrator = null;

    public IntegrityDatabaseManager(DatabaseSpecifics databaseSpecifics) {
        this.databaseSpecifics = databaseSpecifics;
    }

    @Override
    protected DatabaseSpecifics getDatabaseSpecifics() {
        return databaseSpecifics;
    }

    @Override
    protected synchronized DatabaseMigrator getMigrator() {
        if (migrator == null) {
            migrator = new IntegrityDatabaseMigrator(connector);
        }
        return migrator;
    }

    @Override
    protected boolean needsMigration() {
        return getMigrator().needsMigration();
    }

    @Override
    protected String getDatabaseCreationScript() {
        return INTEGRITY_SERVICE_DATABASE_SCHEMA;
    }

}
