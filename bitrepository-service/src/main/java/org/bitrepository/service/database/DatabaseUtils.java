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
package org.bitrepository.service.database;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DatabaseUtils {
    private static final Logger log = LoggerFactory.getLogger(DatabaseUtils.class);
    public static final String DERBY_EMBEDDED_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private DatabaseUtils() {}

    /**
     * Retrieves an integer value from the database through the use of a SQL query, which requests
     * the wanted integer value, and the arguments for the query to become a proper SQL statement.
     *
     * @param dbConnector For connecting to the database.
     * @param query       The query for retrieving the integer value.
     * @param args        The arguments for the database statement.
     * @return The integer value from the given statement.
     * @throws IllegalStateException if the connection could not be established,
     *                               if the prepared statement could not be created, or the execution failed
     */
    public static Integer selectIntValue(DBConnector dbConnector, String query, Object... args) {
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
        ArgumentValidator.checkNotNullOrEmpty(query, "String query");
        ArgumentValidator.checkNotNull(args, "Object... args");

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = createPreparedStatement(conn, query, args)) {
            try (ResultSet res = ps.executeQuery()) {
                if (!res.next()) {
                    throw new IllegalStateException("No results from " + ps);
                }
                Integer resultInt = res.getInt(1);
                if (res.wasNull()) {
                    resultInt = null;
                }
                if (res.next()) {
                    throw new IllegalStateException("Too many results from " + ps);
                }
                return resultInt;
            }
        } catch (SQLException e) {
            throw failedExecutionOfStatement(e, query, args);
        }
    }

    /**
     * Retrieves a long value from the database through the use of a SQL query, which requests
     * the wanted long value, and the arguments for the query to become a proper SQL statement.
     *
     * @param dbConnector For connecting to the database.
     * @param query       The query for retrieving the long value.
     * @param args        The arguments for the database statement.
     * @return The long value from the given statement.
     * @throws IllegalStateException if the connection could not be established,
     *                               if the prepared statement could not be created, or the execution failed
     */
    public static Long selectLongValue(DBConnector dbConnector, String query, Object... args) {
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
        ArgumentValidator.checkNotNullOrEmpty(query, "String query");
        ArgumentValidator.checkNotNull(args, "Object... args");

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = createPreparedStatement(conn, query, args)) {
            try (ResultSet res = ps.executeQuery()) {
                if (!res.next()) {
                    log.trace("Got an empty result set for statement '" + query + "' with arguments '"
                            + Arrays.asList(args) + "' on database '" + conn + "'. Returning a null.");
                    return null;
                }
                Long resultLong = res.getLong(1);
                if (res.wasNull()) {
                    resultLong = null;
                }
                if (res.next()) {
                    throw new IllegalStateException("Too many results from " + ps);
                }
                return resultLong;
            }
        } catch (SQLException e) {
            throw failedExecutionOfStatement(e, query, args);
        }
    }

    /**
     * Retrieves the first long value from the database through the use of a SQL query and the arguments for the query
     * to become a proper SQL statement, which requests the wanted set of long values, where only the first is
     * returned.
     *
     * @param dbConnector For connecting to the database.
     * @param query       The query for retrieving the long value.
     * @param args        The arguments for the database statement.
     * @return The long value from the given statement.
     * @throws IllegalStateException if the connection could not be established,
     *                               if the prepared statement could not be created, or the execution failed
     */
    public static Long selectFirstLongValue(DBConnector dbConnector, String query, Object... args) {
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
        ArgumentValidator.checkNotNullOrEmpty(query, "String query");
        ArgumentValidator.checkNotNull(args, "Object... args");

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = createPreparedStatement(conn, query, args)) {
            try (ResultSet res = ps.executeQuery()) {
                if (!res.next()) {
                    log.trace("Got an empty result set for statement '" + query + "' with arguments '"
                            + Arrays.asList(args) + "' on database '" + conn + "'. Returning a null.");
                    return null;
                }
                Long resultLong = res.getLong(1);
                if (res.wasNull()) {
                    resultLong = null;
                }
                return resultLong;
            }
        } catch (SQLException e) {
            throw failedExecutionOfStatement(e, query, args);
        }
    }

    /**
     * Retrieves the first string value from the database through the use of a SQL query and the arguments for the query
     * to become a proper SQL statement, which requests the wanted set of string values, where only the first is
     * returned.
     *
     * @param dbConnector For connecting to the database.
     * @param query       The query for retrieving the long value.
     * @param args        The arguments for the database statement.
     * @return The string value from the given statement.
     * @throws IllegalStateException if the connection could not be established,
     *                               if the prepared statement could not be created, or the execution failed
     */
    public static String selectFirstStringValue(DBConnector dbConnector, String query, Object... args) {
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
        ArgumentValidator.checkNotNullOrEmpty(query, "String query");
        ArgumentValidator.checkNotNull(args, "Object... args");

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = createPreparedStatement(conn, query, args)) {
            try (ResultSet res = ps.executeQuery()) {
                if (!res.next()) {
                    log.trace("Got an empty result set for statement '" + query + "' with arguments '"
                            + Arrays.asList(args) + "' on database '" + conn + "'. Returning a null.");
                    return null;
                }
                String resultString = res.getString(1);
                if (res.wasNull()) {
                    resultString = null;
                }
                return resultString;
            }
        } catch (SQLException e) {
            throw failedExecutionOfStatement(e, query, args);
        }
    }

    /**
     * Retrieves a list of long values from the database through the use of a SQL query, which requests
     * the wanted long values, and the arguments for the query to become a proper SQL statement.
     *
     * @param dbConnector For connecting to the database.
     * @param query       The query for retrieving the long value.
     * @param args        The arguments for the database statement.
     * @return The list of long values from the given statement.
     * @throws IllegalStateException if the connection could not be established,
     *                               if the prepared statement could not be created, or the execution failed
     */
    public static List<Long> selectLongList(DBConnector dbConnector, String query, Object... args) {
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
        ArgumentValidator.checkNotNullOrEmpty(query, "String query");
        ArgumentValidator.checkNotNull(args, "Object... args");

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = createPreparedStatement(conn, query, args)) {
            List<Long> list = new ArrayList<>();
            try (ResultSet res = ps.executeQuery()) {
                while (res.next()) {
                    list.add(res.getLong(1));
                }
                return list;
            }
        } catch (SQLException e) {
            throw failedExecutionOfStatement(e, query, args);
        }
    }

    /**
     * Retrieves an date value from the database through the use of a SQL query, which requests
     * the wanted date value, and the arguments for the query to become a proper SQL statement.
     *
     * @param dbConnector For connecting to the database.
     * @param query       The query for retrieving the date.
     * @param args        The arguments for the database statement.
     * @return The date from the given statement.
     * @throws IllegalStateException if the connection could not be established,
     *                               if the prepared statement could not be created, or the execution failed
     */
    public static Date selectDateValue(DBConnector dbConnector, String query, Object... args) {
        return retrieveDateValue(dbConnector, true, query, args);
    }

    /**
     * Retrieves the first date value from the database through the use of a SQL query, which requests
     * the wanted date value, and the arguments for the query to become a proper SQL statement.
     * If the results set contains more than one value, the others are ignored.
     *
     * @param dbConnector For connecting to the database.
     * @param query       The query for retrieving the date.
     * @param args        The arguments for the database statement.
     * @return The date from the given statement.
     * @throws IllegalStateException if the connection could not be established,
     *                               if the prepared statement could not be created, or the execution failed
     */
    public static Date selectFirstDateValue(DBConnector dbConnector, String query, Object... args) {
        return retrieveDateValue(dbConnector, false, query, args);
    }

    /**
     * The actual extraction from the database.
     *
     * @param dbConnector           For connecting to the database.
     * @param mustHaveOnlyOneResult Whether it should fail, if more than one result is found.
     * @param query                 The query for retrieving the date.
     * @param args                  The arguments for the database statement.
     * @return The date from the given statement.
     * @throws IllegalStateException if the connection could not be established,
     *                               if the prepared statement could not be created, or the execution failed
     */
    private static Date retrieveDateValue(DBConnector dbConnector, boolean mustHaveOnlyOneResult, String query, Object... args) {
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
        ArgumentValidator.checkNotNullOrEmpty(query, "String query");
        ArgumentValidator.checkNotNull(args, "Object... args");

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = createPreparedStatement(conn, query, args)) {
            try (ResultSet res = ps.executeQuery()) {
                if (!res.next()) {
                    log.trace("Got an empty result set for statement '" + query + "' on database '"
                            + conn + "'. Returning a null.");
                    return null;
                }
                Timestamp resultDate = res.getTimestamp(1);
                if (res.wasNull()) {
                    resultDate = null;
                }
                if (mustHaveOnlyOneResult && res.next()) {
                    throw new IllegalStateException("Too many results from " + ps);
                }
                return resultDate;
            }
        } catch (SQLException e) {
            throw failedExecutionOfStatement(e, query, args);
        }
    }

    /**
     * Retrieves a String value from the database through the use of a SQL query, which requests
     * the wanted String value, and the arguments for the query to become a proper SQL statement.
     *
     * @param dbConnector For connecting to the database.
     * @param query       The query to extract the String value.
     * @param args        The arguments for the statement.
     * @return The requested string value, or null if no such value could be found.
     * @throws IllegalStateException if the connection could not be established,
     *                               if the prepared statement could not be created, or the execution failed
     */
    public static String selectStringValue(DBConnector dbConnector, String query, Object... args) {
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
        ArgumentValidator.checkNotNullOrEmpty(query, "String query");
        ArgumentValidator.checkNotNull(args, "Object... args");

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = createPreparedStatement(conn, query, args)) {
            try (ResultSet res = ps.executeQuery()) {
                if (!res.next()) {
                    log.trace("No string was found for the query '" + query + "'. A null has been returned.");
                    return null;
                }
                return res.getString(1);
            }
        } catch (SQLException e) {
            throw failedExecutionOfStatement(e, query, args);
        }
    }

    /**
     * Retrieves a list of String value from the database through the use of a SQL query, which requests
     * the wanted list of String value, and the arguments for the query to become a proper SQL statement.
     *
     * @param dbConnector For connecting to the database.
     * @param query       The SQL query for retrieving the strings.
     * @param args        The arguments for the statement.
     * @return The requested list of strings. If no strings were found, then the list is empty.
     * @throws IllegalStateException if the connection could not be established,
     *                               if the prepared statement could not be created, or the execution failed
     */
    public static List<String> selectStringList(DBConnector dbConnector, String query, Object... args) {
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
        ArgumentValidator.checkNotNullOrEmpty(query, "String query");
        ArgumentValidator.checkNotNull(args, "Object... args");

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = createPreparedStatement(conn, query, args)) {
            try (ResultSet res = ps.executeQuery()) {
                List<String> list = new ArrayList<>();
                while (res.next()) {
                    list.add(res.getString(1));
                }
                return list;
            }
        } catch (SQLException e) {
            throw failedExecutionOfStatement(e, query, args);
        }
    }

    /**
     * Executing a given statement, which should not return any results.
     * This is intended to be used especially for UPDATE commands.
     *
     * @param dbConnector For connecting to the database.
     * @param query       The SQL query to execute.
     * @param args        The arguments for the SQL statement.
     * @throws IllegalStateException if the connection could not be established,
     *                               if the prepared statement could not be created, or the execution failed
     */
    public static void executeStatement(DBConnector dbConnector, String query, Object... args) throws IllegalStateException {
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
        ArgumentValidator.checkNotNullOrEmpty(query, "String query");
        ArgumentValidator.checkNotNull(args, "Object... args");

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = createPreparedStatement(conn, query, args)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw failedExecutionOfStatement(e, query, args);
        }
    }

    /**
     * Prepare a statement given a query string and some args.
     * <p>
     * NB: the provided connection is not closed.
     *
     * @param dbConnection The connection to the database.
     * @param query        a query string  (must not be null or empty)
     * @param args         some args to insert into this query string (must not be null)
     * @return a prepared statement
     * @throws SQLException          If unable to prepare a statement
     * @throws IllegalStateException if any of the args is of an unknown type
     */
    public static PreparedStatement createPreparedStatement(Connection dbConnection, String query, Object... args)
            throws SQLException {
        log.trace("Preparing the statement: '" + query + "' with arguments '" + Arrays.asList(args) + "'");
        PreparedStatement s = dbConnection.prepareStatement(query);
        int i = 1;
        for (Object arg : args) {
            if (arg instanceof String) {
                s.setString(i, (String) arg);
            } else if (arg instanceof Integer) {
                s.setInt(i, (Integer) arg);
            } else if (arg instanceof Long) {
                s.setLong(i, (Long) arg);
            } else if (arg instanceof Boolean) {
                s.setBoolean(i, (Boolean) arg);
            } else if (arg instanceof Date) {
                s.setTimestamp(i, new Timestamp(((Date) arg).getTime()));
            } else {
                if (arg == null) {
                    throw new IllegalStateException("Cannot handle a null as argument for SQL query. We can only "
                            + "handle string, int, long, date or boolean args for query: " + query);
                } else {
                    throw new IllegalStateException("Cannot handle type '" + arg.getClass().getName() + "'. We can only "
                            + "handle string, int, long, date or boolean args for query: " + query);
                }
            }
            i++;
        }

        return s;
    }

    /**
     * @param e     The throwable exception for the execution that failed.
     * @param query The SQL query for the statement, which caused the failure.
     * @param args  The arguments for the statement, which caused the failure.
     * @return An {@link IllegalStateException} saying that the given query could not be executed with the given arguments.
     */
    private static IllegalStateException failedExecutionOfStatement(Throwable e, String query, Object... args) {
        return new IllegalStateException("Could not execute the query '" + query + "' with the arguments '"
                + Arrays.asList(args) + "'", e);
    }

    /**
     * Method to provide safe dump of database specifics (so we won't leak passwords).
     *
     * @param databaseSpecifics the specifics of the database, containing sensitive passwords
     * @return the cleaned database specifics as a string
     */
    public static String getDatabaseSpecificsDump(DatabaseSpecifics databaseSpecifics) {
        return "DatabaseSpecifics [driverClass=" + databaseSpecifics.getDriverClass()
                + ", databaseURL=" + databaseSpecifics.getDatabaseURL()
                + ", username=" + databaseSpecifics.getUsername()
                + ", allowAutoMigrate=" + databaseSpecifics.isAllowAutoMigrate()
                + ", allowAutoCreate=" + databaseSpecifics.isAllowAutoCreate() + "]";
    }
}
