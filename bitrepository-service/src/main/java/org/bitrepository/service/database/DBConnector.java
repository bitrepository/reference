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
package org.bitrepository.service.database;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.DataSources;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnector {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final DatabaseSpecifics databaseSpecifics;
    private final ComboPooledDataSource connectionPool;

    /**
     * @param databaseSpecifics The specifics for the configuration of the database.
     */
    public DBConnector(DatabaseSpecifics databaseSpecifics) {
        ArgumentValidator.checkNotNull(databaseSpecifics, "DatabaseSpecifics specifics");

        silenceC3P0Logger();
        this.databaseSpecifics = databaseSpecifics;
        this.connectionPool = new ComboPooledDataSource();
        initialiseConnectionPool();
    }

    /**
     * @return The class for the driver for the database.
     */
    public String getDatabaseDriverClass() {
        return databaseSpecifics.getDriverClass();
    }

    /**
     * Initialises the ConnectionPool for the connections to the database.
     */
    private void initialiseConnectionPool() {
        try {
            log.info("Creating the connection to the database '" + DatabaseUtils.getDatabaseSpecificsDump(databaseSpecifics) + "'.");
            connectionPool.setDriverClass(databaseSpecifics.getDriverClass());
            connectionPool.setJdbcUrl(databaseSpecifics.getDatabaseURL());
            if (databaseSpecifics.isSetUsername()) {
                connectionPool.setUser(databaseSpecifics.getUsername());
            }
            if (databaseSpecifics.isSetPassword()) {
                connectionPool.setPassword(databaseSpecifics.getPassword());
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Could not connect to the database '" + DatabaseUtils.getDatabaseSpecificsDump(databaseSpecifics) + "'", e);
        }
    }

    /**
     * Hack to kill com.mchange.v2 log spamming.
     */
    private void silenceC3P0Logger() {
        Properties p = new Properties(System.getProperties());
        p.put("com.mchange.v2.log.MLog", "com.mchange.v2.log.FallbackMLog");
        p.put("com.mchange.v2.log.FallbackMLog.DEFAULT_CUTOFF_LEVEL", "OFF"); // or any other
        System.setProperties(p);
    }

    /**
     * Creates and connects to the database.
     *
     * @return The connection to the database.
     * @throws IllegalStateException if the database connection could not be established
     */
    public Connection getConnection() throws IllegalStateException {
        try {
            return connectionPool.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not establish connection to the database: '" + DatabaseUtils.getDatabaseSpecificsDump(databaseSpecifics)
                            + "'", e);
        }
    }

    /**
     * Cleans up after use.
     */
    public void destroy() {
        try {
            DataSources.destroy(connectionPool);
        } catch (SQLException e) {
            log.error("Could not clean up the database '" + DatabaseUtils.getDatabaseSpecificsDump(databaseSpecifics) + "'.", e);
        }
    }
}
