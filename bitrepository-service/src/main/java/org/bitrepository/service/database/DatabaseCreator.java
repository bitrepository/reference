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

package org.bitrepository.service.database;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DatabaseCreator is a DatabaseMaintainer which knows how to create a database from a given script.
 */

public class DatabaseCreator extends DatabaseMaintainer {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Creates a database by running the supplied script.
     *
     * @param scriptName        The name of the script including path as part of the
     *                          classpath.
     * @param databaseSpecifics Specifies where to create the database.
     */
    protected void createDatabase(DatabaseSpecifics databaseSpecifics, String scriptName) {
        DatabaseSpecifics databaseCreationSpecifics = updateDatabaseSpecificsForDBCreation(databaseSpecifics);
        log.info("Creating database in " + DatabaseUtils.getDatabaseSpecificsDump(databaseCreationSpecifics)
                + " from script " + scriptName);
        try {
            runScript(databaseCreationSpecifics, scriptName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Appends the specified database url with <code>;create=true</code> to allow creation of the database.
     *
     * @param databaseSpecifics Specifies where to create the database.
     * @return DatabaseSpecifics instance with ";create=true" added (Derby specific).
     */
    private static DatabaseSpecifics updateDatabaseSpecificsForDBCreation(DatabaseSpecifics databaseSpecifics) {
        DatabaseSpecifics newDatabaseSpecifics = new DatabaseSpecifics();
        newDatabaseSpecifics.setDriverClass(databaseSpecifics.getDriverClass());
        newDatabaseSpecifics.setDatabaseURL(databaseSpecifics.getDatabaseURL() + ";create=true");
        newDatabaseSpecifics.setUsername(databaseSpecifics.getUsername());
        newDatabaseSpecifics.setPassword(databaseSpecifics.getPassword());
        return newDatabaseSpecifics;
    }

    protected Settings loadSettings(String pillarID, String pathToSettings) {
        SettingsProvider settingsLoader =
                new SettingsProvider(new XMLFileSettingsLoader(pathToSettings), pillarID);

        return settingsLoader.getSettings();
    }
}
