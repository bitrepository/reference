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

package org.bitrepository.pillar.common;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.database.DatabaseCreator;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * Will create a checksum database.
 * Defines the concrete DatabaseSpecifics and sql script location,
 * all intelligence is located in the DatabaseCreator super class.
 *
 * @see DatabaseCreator
 */
public class ChecksumDatabaseCreator extends DatabaseCreator {
    /** Default location for the script for creating the Checksum database.*/
    public static final String DEFAULT_CHECKSUM_DB_SCRIPT = "sql/derby/checksumDBCreation.sql";

    /**
     * @param args <ol>
     * <li> The path to the directory containing the settings. See
     * {@link org.bitrepository.common.settings.XMLFileSettingsLoader} for details.</li>
     * <li> The database creation script as found in the classpath. Will revert to  DEFAULT_CHECKSUM_DB_SCRIPT =
     * "sql/derby/checksumDBCreation.sql" if null.</li>
     * </ol>
     */
    public static void main(String[] args) {
        ChecksumDatabaseCreator dbCreator = new ChecksumDatabaseCreator();
        Settings settings = dbCreator.loadSettings(null, args[0]);

        dbCreator.createChecksumDatabase(settings, args[1]);
    }

    public void createChecksumDatabase(Settings settings, String pathToSqlCreationScript) {
        DatabaseSpecifics databaseSpecifics =
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase();

        createDatabase(databaseSpecifics, pathToSqlCreationScript == null ? 
                DEFAULT_CHECKSUM_DB_SCRIPT : pathToSqlCreationScript);
    }
}
