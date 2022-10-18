/*
 * #%L
 * Bitrepository Service
 * %%
 * Copyright (C) 2010 - 2015 The State and University Library, The Royal Library and The State Archives, Denmark
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * General class for obtaining a database connection to a service's database.
 * The manager is responsible for:
 * - Creating a new database, if none existed on the connection url (derby only).
 * - Connecting to the database.
 * - Migrating the database schema if needed (derby only).
 * <p>
 * If the database is unavailable exceptions will be thrown detailing the reason.
 * Databases that are not automatically migrated will result in an exception.
 */
public abstract class DatabaseManager {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private static final String derbyDriver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String postgresDriver = "org.postgresql.Driver";
    protected DBConnector connector = null;

    /**
     * Method to obtain the DBConnector for the concrete instance of the database.
     * When a connection is returned, the database is ensured to be existing, connected
     * and in the expected version.
     *
     * @return {@link DBConnector} DBConnector to the database.
     */
    public DBConnector getConnector() {
        DatabaseSpecifics ds = getDatabaseSpecifics();
        if (ds.getDriverClass().equals(derbyDriver)) {
            return getDerbyConnection();
        } else if (ds.getDriverClass().equals(postgresDriver)) {
            return getPostgresConnection();
        } else {
            throw new IllegalStateException("The database driver: '" + ds.getDriverClass() + "' is not supported."
                    + " Supported drivers are: '" + derbyDriver + "' and '" + postgresDriver + "'.");
        }


    }

    /**
     * Get a connection to a postgres database
     * This method won't attempt to create a database if connection is unsuccessful,
     * also it won't attempt to migrate the database.
     *
     * @return {@link DBConnector} The connector to the database
     */
    private DBConnector getPostgresConnection() {
        if (connector == null) {
            obtainConnection();
        }
        if (needsMigration()) {
            throw new IllegalStateException("Automatic migration of postgres databases are not supported. " +
                    "The database at '" + getDatabaseSpecifics().getDatabaseURL() +
                    "' needs manual migration.");
        }

        return connector;
    }

    /**
     * Get a connection to a derby database
     * Creates the database if the connection to it fails. Migrates the database schema if it is needed.
     *
     * @return {@link DBConnector} The connector to the database
     */
    private DBConnector getDerbyConnection() {
        if (connector == null) {
            try {
                obtainConnection();
            } catch (IllegalStateException e) {
                if (allowAutoCreate()) {
                    log.warn("Failed to connect to database, attempting to create it");
                    createDatabase();
                } else {
                    log.error("Failed to connect to database, auto-creation is disabled");
                    throw e;
                }
            }
            if (connector == null) {
                obtainConnection();
            }
            log.info("Checking if the database needs to be migrated.");
            if (needsMigration()) {
                if (allowAutoMigrate()) {
                    log.warn("Database needs to be migrated, attempting to auto-migrate.");
                    migrateDatabase();
                } else {
                    log.error("Database needs migration, auto-migrations is disabled.");
                    throw new IllegalStateException("Database needs migration, auto-migrations is disabled.");
                }
            } else {
                log.info("Database migration was not needed.");
            }
        }
        return connector;
    }

    /**
     * Obtain the database specifics for the database to manage.
     *
     * @return DatabaseSpecifics The database specifics for the concrete database
     */
    protected abstract DatabaseSpecifics getDatabaseSpecifics();

    /**
     * Get the migrator for the concrete database.
     *
     * @return DatabaseMigrator The concrete database migrator
     */
    protected abstract DatabaseMigrator getMigrator();

    /**
     * Method to determine whether the database needs to be migrated.
     *
     * @return true if migration should be done, false otherwise
     */
    protected abstract boolean needsMigration();

    /**
     * Get the path to the file containing the full database schema for initial creation of
     * the concrete database.
     *
     * @return String The path to the file with the database in String form.
     */
    protected abstract String getDatabaseCreationScript();

    /**
     * Connect to a database using the database specifics from the implementing class.
     *
     * @throws IllegalStateException if connection cannot be obtained.
     */
    protected void obtainConnection() throws IllegalStateException {
        log.debug("Obtaining db connection.");
        connector = new DBConnector(getDatabaseSpecifics());
        Connection connection = connector.getConnection();
        try {
            connection.close();
        } catch (SQLException e) {
            log.warn("Connection opened for testing connectivity failed to close", e);
        }
        log.debug("Obtained db connection.");
    }

    /**
     * Perform the actual migration of the concrete database.
     */
    private void migrateDatabase() {
        DatabaseMigrator migrator = getMigrator();
        if (migrator != null) {
            migrator.migrate();
        } else {
            throw new IllegalStateException("The database was attempted migrated, but no migrator was available.");
        }
    }

    private boolean allowAutoMigrate() {
        DatabaseSpecifics ds = getDatabaseSpecifics();
        if (ds.isSetAllowAutoMigrate()) {
            return ds.isAllowAutoMigrate();
        } else {
            return true;
        }
    }

    private boolean allowAutoCreate() {
        DatabaseSpecifics ds = getDatabaseSpecifics();
        if (ds.isSetAllowAutoCreate()) {
            return ds.isAllowAutoCreate();
        } else {
            return true;
        }
    }

    /**
     * Create the database on the given database specifics.
     */
    private void createDatabase() {
        DatabaseCreator databaseCreator = new DatabaseCreator();
        databaseCreator.createDatabase(getDatabaseSpecifics(), getDatabaseCreationScript());
    }

}
