/*
 * #%L
 * Bitrepository Common
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

import java.sql.Connection;
import java.sql.SQLException;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;

/**
 * The connector to a database.
 */
public class DBConnector {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The specifications for connection to the database.*/
    private final DatabaseSpecifics databaseSpecifics;
    /** The pool with data sources for the database connections.*/
    private ComboPooledDataSource connectionPool;
    
    /**
     * Constructor.
     * @param specifics The specifics for the database.
     * @param url The URL for the database.
     */
    public DBConnector(DatabaseSpecifics databaseSpecifics) {
        ArgumentValidator.checkNotNull(databaseSpecifics, "DatabaseSpecifics specifics");
        
        this.databaseSpecifics = databaseSpecifics;
        this.connectionPool = new ComboPooledDataSource();
        
        initialiseConnection();
    }
    
    /**
     * Initialises the connection to the database.
     */
    private void initialiseConnection() {
        try {
            log.info("Creating the connection to the database '" + databaseSpecifics + "'.");
            connectionPool.setDriverClass(databaseSpecifics.getDriverClass());
            connectionPool.setJdbcUrl(databaseSpecifics.getDatabaseURL());
            if(databaseSpecifics.isSetUsername()) {
                connectionPool.setUser(databaseSpecifics.getUsername());
            }
            if(databaseSpecifics.isSetPassword()) {
                connectionPool.setPassword(databaseSpecifics.getPassword());
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not connect to the database '" + databaseSpecifics + "'", e);
        }
    }
    
    /**
     * Creates and connects to the database.
     * @return The connection to the database.
     */
    public Connection getConnection() {
        try {
            return connectionPool.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not establish connection to the database: '" + databaseSpecifics 
                    + "'", e);
        }
    }
    
    /**
     * Cleans up after use.
     */
    public void cleanup() {
        try {
            DataSources.destroy(connectionPool);
        } catch (SQLException e) {
            log.error("Could not clean up the database '" + databaseSpecifics + "'.", e);
        }
    }
}
