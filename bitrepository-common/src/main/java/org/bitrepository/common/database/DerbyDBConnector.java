/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.common.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.derby.tools.ij;
import org.bitrepository.common.ArgumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the connection to a Derby database.
 * Currently only handles the embedded version of the Derby database. 
 */
public class DerbyDBConnector implements DBConnector {
    /** The pool of connections.*/
    private static Map<Thread, Connection> connectionPool
            = new WeakHashMap<Thread, Connection>();
    /** The log.*/
    private static Logger log = LoggerFactory.getLogger(DerbyDBConnector.class);

    /** The embedded derby driver path.*/
    private static final String DERBY_EMBEDDED_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    /** A default value for the check timeout.*/
    private static final int validityCheckTimeout = 10000;
    
    @Override
    public Connection getEmbeddedDBConnection(String dbUrl) {
        ArgumentValidator.checkNotNullOrEmpty(dbUrl, "String dbUrl");

        try {
            Connection connection = connectionPool.get(Thread.currentThread());
            boolean renew = ((connection == null) 
                    || (!connection.isValid(validityCheckTimeout)));
            if (renew) {  
                Class.forName(DERBY_EMBEDDED_DRIVER);
                connection = DriverManager.getConnection(dbUrl);
                connection.setAutoCommit(false);
                connectionPool.put(Thread.currentThread(), connection);
                log.info("Connected to database using DBurl '"
                        + dbUrl + "'  using driver '" + DERBY_EMBEDDED_DRIVER + "'");
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Can't find driver '" + DERBY_EMBEDDED_DRIVER + "'", e);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot instantiate the connection to the database.", e);
        }
    }
    
    @Override
    public void createDatabase(File sqlDatabaseFile) {
        if(!sqlDatabaseFile.isFile()) {
            throw new IllegalStateException("Could not find the file with the sql for the database at '" 
                    + sqlDatabaseFile.getAbsolutePath() + "'.");
        }
        
        // Use the derby tool 'ij' to create the database.
        try {
            ij.main(new String[]{sqlDatabaseFile.getAbsolutePath()});
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate the database.");
        }
    }
}
