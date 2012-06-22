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
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.bitrepository.common.ArgumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The connector to a database.
 */
public class DBConnector {
    /** The pool of connections.*/
    private Map<Thread, Connection> connectionPool = Collections.synchronizedMap(new WeakHashMap<Thread, Connection>());
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** A default value for the check timeout.*/
    private static final int validityCheckTimeout = 10000;
    
    /** The URL to the database.*/
    private final String dbUrl;
    /** The specifics for the driver to the database.*/
    private final DBSpecifics specifics;
    
    /**
     * Constructor.
     * @param specifics The specifics for the database.
     * @param url The URL for the database.
     */
    public DBConnector(DBSpecifics specifics, String url) {
        ArgumentValidator.checkNotNull(specifics, "DBSpecifics specifics");
        ArgumentValidator.checkNotNullOrEmpty(url, "String url");
        
        this.dbUrl = url;
        this.specifics = specifics;
    }
    
    /**
     * Creates and connects to the database.
     * @return The connection to the database.
     */
    public Connection getConnection() {
        try {
            Connection connection = connectionPool.get(Thread.currentThread());
            boolean renew = ((connection == null) 
                    || (!connection.isValid(validityCheckTimeout)));
            if (renew) {  
                Class.forName(specifics.getDriverClassName());
                connection = DriverManager.getConnection(dbUrl);
                connection.setAutoCommit(false);
                connectionPool.put(Thread.currentThread(), connection);
                log.info("Connected to database using DBurl '"
                        + dbUrl + "'  using driver '" + specifics.getDriverClassName() + "'");
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Can't find driver '" + specifics.getDriverClassName() + "'", e);
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot instantiate the connection to the database.", e);
        }
    }
}
