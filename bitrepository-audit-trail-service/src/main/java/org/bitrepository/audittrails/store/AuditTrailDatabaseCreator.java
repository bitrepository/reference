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

package org.bitrepository.audittrails.store;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.database.DatabaseCreator;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * Used for creating audit trail service databases. Will just execute the provided sql script on the
 * audit trail database as defined in the provided settings.
 */
public class AuditTrailDatabaseCreator extends DatabaseCreator {
    public static final String DEFAULT_AUDIT_TRAIL_DB_SCRIPT = "sql/derby/auditTrailServiceDBCreation.sql";


    /**
     * @param args <ol>
     * <li> The path to the directory containing the settings. See
     * {@link org.bitrepository.common.settings.XMLFileSettingsLoader} for details.</li>
     * <li> The database creation script as found in the classpath. Will revert to DEFAULT_AUDIT_TRAIL_DB_SCRIPT =
     * "sql/derby/checksumDBCreation.sql" if null.</li>
     * </ol>
     */
    public static void main(String[] args) {
        AuditTrailDatabaseCreator dbCreator = new AuditTrailDatabaseCreator();
        Settings settings = dbCreator.loadSettings(null, args[0]);

        dbCreator.createAuditTrailDatabase(settings, args[1]);
    }

    public void createAuditTrailDatabase(Settings settings, String pathToSqlCreationScript) {
        DatabaseSpecifics databaseSpecifics =
                settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailServiceDatabase();

        if (pathToSqlCreationScript == null) {
            pathToSqlCreationScript = DEFAULT_AUDIT_TRAIL_DB_SCRIPT;
        }

        createDatabase(databaseSpecifics, pathToSqlCreationScript);
    }
}