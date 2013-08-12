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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

public class DatabaseMaintainer {

    /**
     * Attempts to find a script with the indicated name in the classpath and run it.
     * @param scriptName The name of the script in the classpath.
     */
    protected static void runScript(DatabaseSpecifics databaseSpecifics, String scriptName) throws Exception {
        Connection connection = getDBConnection(databaseSpecifics);
        runScript(connection, scriptName);
        connection.close();
    }
    
    /**
     * Attempts to find a script with the indicated name in the classpath and run it.
     * @param scriptName The name of the script in the classpath.
     */
    protected static void runScript(DBConnector connector, String scriptName) throws Exception {
        Connection connection = connector.getConnection();
        runScript(connection, scriptName);
        connection.close();
    }
    
    /**
     * 
     * @param connection
     * @param scriptName
     * @throws Exception
     */
    private static void runScript(Connection connection, String scriptName) throws Exception {
        SqlScriptRunner scriptRunner = new SqlScriptRunner(connection, false, true);
        scriptRunner.runScript(getReaderForFile(scriptName));
    }

    /**
     * Creates a reader for the file found at indicated location.
     */
    private static Reader getReaderForFile(String filePath) throws java.io.IOException {
        InputStream is = DatabaseMaintainer.class.getClassLoader().getResourceAsStream(filePath);

        if (is == null) {
            throw new RuntimeException("Didn't find any file in classpath corresponding to " + filePath);
        }

        return new BufferedReader(new InputStreamReader(is));
    }

    /**
     * Creates a connection based on the supplied <code>databaseSpecifics</code>.
     */
    protected static Connection getDBConnection(DatabaseSpecifics databaseSpecifics) throws Exception {
        Class.forName(databaseSpecifics.getDriverClass());
        Connection connection = DriverManager.getConnection(databaseSpecifics.getDatabaseURL());
        return connection;
    }
}
