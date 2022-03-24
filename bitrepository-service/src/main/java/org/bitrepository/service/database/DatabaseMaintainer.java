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

import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseMaintainer knows how to connect to a database and run scripts against it.
 */
public class DatabaseMaintainer {

    /**
     * Attempts to find a script with the indicated name in the classpath and run it.
     *
     * @param databaseSpecifics the specifics of the database
     * @param scriptName        The name of the script in the classpath.
     * @throws SQLException           if a connection to the database could not be established or if it failed to close the connection
     * @throws ClassNotFoundException if the database driver is not on the classpath
     * @throws IOException            if the script can not be read
     * @throws FileNotFoundException  if the script is not on the classpath
     * @throws RuntimeException       if the script can not be run
     */
    protected static void runScript(DatabaseSpecifics databaseSpecifics, String scriptName) throws SQLException,
            ClassNotFoundException, FileNotFoundException, IOException {
        Connection connection = getDBConnection(databaseSpecifics);
        runScript(connection, scriptName);
        connection.close();
    }

    /**
     * Attempts to find a script with the indicated name in the classpath and run it.
     *
     * @param connector  the database connector
     * @param scriptName The name of the script in the classpath.
     * @throws IOException           if the script could not be read or found
     * @throws IllegalStateException if the database connection could not be opened
     * @throws SQLException          if the database connection could not be closed
     * @throws RuntimeException      if the script failed
     * @see #runScript(Connection, String)
     */
    protected static void runScript(DBConnector connector, String scriptName) throws IOException, IllegalStateException,
            SQLException, RuntimeException {
        Connection connection = connector.getConnection();
        runScript(connection, scriptName);
        connection.close();
    }

    /**
     * Run the script given.
     *
     * @param connection the database connection
     * @param scriptName the name of the script
     * @throws IOException      if the script could not be read
     * @throws RuntimeException if the script could not be run
     * @see SqlScriptRunner#runScript(Reader)
     */
    private static void runScript(Connection connection, String scriptName) throws IOException, RuntimeException {
        SqlScriptRunner scriptRunner = new SqlScriptRunner(connection, false, true);
        scriptRunner.runScript(getReaderForFile(scriptName));
    }

    /**
     * Creates a reader for the file found at indicated location.
     *
     * @param filePath the path to the file on the classpath
     * @return a reader for the content
     * @throws IOException           if the file cannot be read
     * @throws FileNotFoundException if the file is not on the classpath
     */
    private static Reader getReaderForFile(String filePath) throws FileNotFoundException, java.io.IOException {
        InputStream is = DatabaseMaintainer.class.getClassLoader().getResourceAsStream(filePath);

        if (is == null) {
            throw new FileNotFoundException("Didn't find any file in classpath corresponding to " + filePath);
        }

        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    /**
     * Creates a connection based on the supplied <code>databaseSpecifics</code>.
     *
     * @param databaseSpecifics the database specifics
     * @return the connection to the database
     * @throws SQLException           if a connection to the database could not be established
     * @throws ClassNotFoundException if the specified database driver class could not be found on the classpath
     */
    protected static Connection getDBConnection(DatabaseSpecifics databaseSpecifics) throws SQLException, ClassNotFoundException {
        Class.forName(databaseSpecifics.getDriverClass());
        Connection connection = DriverManager.getConnection(databaseSpecifics.getDatabaseURL());
        return connection;
    }
}
