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

import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database destroyer which knows how to tell derby to delete a database.
 */

public class DerbyDatabaseDestroyer {
    private static final Logger log = LoggerFactory.getLogger(DerbyDatabaseDestroyer.class);

    /** Will delete the database by directly removing the files on disk based on the DB url
     * @param databaseSpecifics necessary information to decide which database.
     * */
    public static void deleteDatabase(DatabaseSpecifics databaseSpecifics) {
        log.info("Removing database: {}", databaseSpecifics);
        String dbUrl = databaseSpecifics.getDatabaseURL();
        String[] dbUrlParts = dbUrl.split(":");
        if (new File(dbUrlParts[2]).isDirectory()) {
            FileUtils.deleteDirIfExists(new File(dbUrlParts[2]));
            restartDatabase(databaseSpecifics);
        }
    }

    private static void restartDatabase(DatabaseSpecifics databaseSpecifics) {
        try {
            DriverManager.getConnection(databaseSpecifics.getDatabaseURL() + ";shutdown=true");
            Class.forName(databaseSpecifics.getDriverClass());
        } catch (SQLException se) {
            //Always throw upon database shutdown, see http://db.apache.org/derby/docs/dev/devguide/tdevdvlp20349.html.
        } catch (Exception e) {
            throw new RuntimeException("Failed to reload database", e);
        }
    }
}
