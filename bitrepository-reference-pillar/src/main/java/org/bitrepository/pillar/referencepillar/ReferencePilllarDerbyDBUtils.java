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
package org.bitrepository.pillar.referencepillar;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import org.bitrepository.common.database.DBConnector;
import org.bitrepository.common.database.SqlScriptRunner;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * Contains functionality for maintenance of the reference pillar databases. This includes functionality for
 * creating and upgrading the databases.
 */
public class ReferencePilllarDerbyDBUtils {
    public static String AUDIT_TRAIL_DB_SCRIPT = "auditContributerDB.sql";
    public static String CHECKSUM_DB_SCRIPT = "checksumDB.sql";

    /** Prevent instantiation this util class */
    private ReferencePilllarDerbyDBUtils() {}

    /**
     * Creates the Derby databses needed by the reference pillar, as specified in the settings.
     */
    public static void createDatabases(Settings settings) throws Exception {
        DatabaseSpecifics auditTrailDB =
                settings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase();
        deleteDatabase(auditTrailDB);
        createDatabase(auditTrailDB, AUDIT_TRAIL_DB_SCRIPT);

        DatabaseSpecifics checksumDB =
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase();
        deleteDatabase(checksumDB);
        createDatabase(checksumDB, CHECKSUM_DB_SCRIPT);
    }

    /**
     * Attempts to find a script with the indicated name in the classpath and run it.
     * @param scriptName The name of the script in the classpath.
     */
    private static void runScript(DatabaseSpecifics databaseSpecifics, String scriptName) throws Exception {
        Connection connection = getEmbeddedDBConnection(databaseSpecifics);
        SqlScriptRunner scriptRunner = new SqlScriptRunner(connection, true, true);
        scriptRunner.runScript(getReaderForFile(scriptName));
    }

    /**
     * Creates a reader for the file found at indicated location.
     */
    private static Reader getReaderForFile(String filePath) throws java.io.IOException {
        return new BufferedReader(
                new InputStreamReader(ReferencePilllarDerbyDBUtils.class.getClassLoader().getResourceAsStream(filePath)));
    }

    /**
     * Appends the specified database url with <code>;create=true</code> to allow creation of the database.
     */
    public static DatabaseSpecifics updateDatabaseSpecificsForDBCreation(DatabaseSpecifics databaseSpecifics) {
        DatabaseSpecifics newDatabaseSpecifics = new DatabaseSpecifics();
        newDatabaseSpecifics.setDriverClass(databaseSpecifics.getDriverClass());
        newDatabaseSpecifics.setDatabaseURL(databaseSpecifics.getDatabaseURL() + ";create=true");
        newDatabaseSpecifics.setUsername(databaseSpecifics.getUsername());
        newDatabaseSpecifics.setPassword(databaseSpecifics.getPassword());
        return newDatabaseSpecifics;
    }

    /**
     * Creates a connection based on the supplied <code>databaseSpecifics</code>.
     */
    private static Connection getEmbeddedDBConnection(DatabaseSpecifics databaseSpecifics) throws Exception {
        Class.forName(databaseSpecifics.getDriverClass());
        Connection connection = DriverManager.getConnection(databaseSpecifics.getDatabaseURL());
        return connection;
    }

    /**
     * Creates a database by running the supplied script.
     * @param databaseSpecifics Specifies where to create the database.
     */
    private static void createDatabase(DatabaseSpecifics databaseSpecifics, String scriptName) throws Exception {
    DBConnector dbConnector = new DBConnector(databaseSpecifics);

        DatabaseSpecifics databaseCreationSpecifics = updateDatabaseSpecificsForDBCreation(databaseSpecifics);
        runScript(databaseCreationSpecifics, scriptName);
    }

    /** Will delete the database by directly removing the files on disk based on the DB url */
    private static void deleteDatabase(DatabaseSpecifics databaseSpecifics) {
        String dbUrl = databaseSpecifics.getDatabaseURL();
        String pathToDatabase = dbUrl.substring(dbUrl.indexOf("derby:") + 6, dbUrl.length());
        FileUtils.deleteDirIfExists(new File(pathToDatabase));
    }
}
