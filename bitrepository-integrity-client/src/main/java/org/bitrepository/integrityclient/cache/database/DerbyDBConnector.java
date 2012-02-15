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
package org.bitrepository.integrityclient.cache.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Map;
import java.util.WeakHashMap;

import org.bitrepository.common.ArgumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DerbyDBConnector {
    /** The pool of connections.*/
    private static Map<Thread, Connection> connectionPool
            = new WeakHashMap<Thread, Connection>();
    /** The log.*/
    private static Logger log = LoggerFactory.getLogger(DerbyDBConnector.class);

    /** The embedded derby driver path.*/
    private static final String DERBY_EMBEDDED_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    /** A default value for the check timeout.*/
    private static final int validityCheckTimeout = 10000;
    
    public static Connection getEmbeddedDBConnection(String dbUrl) throws Exception {
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
            final String message = "Can't find driver '" + DERBY_EMBEDDED_DRIVER + "'";
            log.warn(message, e);
            throw e;
        }
    }
}
