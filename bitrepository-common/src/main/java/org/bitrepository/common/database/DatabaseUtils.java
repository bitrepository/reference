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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bitrepository.common.ArgumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for operating on databases.
 */
public class DatabaseUtils {
    /** The log.*/
    private static Logger log = LoggerFactory.getLogger(DatabaseUtils.class);

    /** Private constructor to prevent instantiation of this utility class.*/
    private DatabaseUtils() { }
    
    /**
     * Retrieves an integer value from the database based on a query and some arguments.
     * @param dbConnection The connection to the database.
     * @param query The query for retrieving the integer value.
     * @param args The arguments for the database statement.
     * @return The integer value from the given statement.
     */
    public static Integer selectIntValue(Connection dbConnection, String query, Object... args) {
        ArgumentValidator.checkNotNull(dbConnection, "Connection dbConnection");
        ArgumentValidator.checkNotNullOrEmpty(query, "String query");
        ArgumentValidator.checkNotNull(args, "Object... args");
        
        try {
            PreparedStatement ps = createPreparedStatement(dbConnection, query, args);
            
            ResultSet res = ps.executeQuery();
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
        } catch (SQLException e) {
            throw new IllegalStateException("Could not retrieve integer value from database '" 
                    + dbConnection + "' with statement '" + query + "'", e);
        }
    }
    
    /**
     * Retrieves a long value from the database based on a query and some arguments.
     * @param dbConnection The connection to the database.
     * @param query The query for retrieving the long value.
     * @param args The arguments for the database statement.
     * @return The long value from the given statement.
     */
    public static Long selectLongValue(Connection dbConnection, String query, Object... args) {
        ArgumentValidator.checkNotNull(dbConnection, "Connection dbConnection");
        ArgumentValidator.checkNotNullOrEmpty(query, "String query");
        ArgumentValidator.checkNotNull(args, "Object... args");
        
        try {
            PreparedStatement ps = createPreparedStatement(dbConnection, query, args);
            
            ResultSet res = ps.executeQuery();
            if (!res.next()) {
                log.info("Got an empty result set for statement '" + query + "' on database '"
                        + dbConnection + "'. Returning a null.");
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
        } catch (SQLException e) {
            throw new IllegalStateException("Could not retrieve long value from database '" 
                    + dbConnection + "' with statement '" + query + "'", e);
        }
    }

    /**
     * Retrieves a date from the database based on a query and some arguments.
     * @param dbConnection The connection to the database.
     * @param query The query for retrieving the date.
     * @param args The arguments for the database statement.
     * @return The date from the given statement.
     */
    public static Date selectDateValue(Connection dbConnection, String query, Object... args) {
        ArgumentValidator.checkNotNull(dbConnection, "Connection dbConnection");
        ArgumentValidator.checkNotNullOrEmpty(query, "String query");
        ArgumentValidator.checkNotNull(args, "Object... args");
        
        try {
            PreparedStatement ps = createPreparedStatement(dbConnection, query, args);
            
            ResultSet res = ps.executeQuery();
            if (!res.next()) {
                log.info("Got an empty result set for statement '" + query + "' on database '"
                        + dbConnection + "'. Returning a null.");
                return null;
            }
            Date resultDate = res.getDate(1);
            if (res.wasNull()) {
                resultDate = null;
            }
            if (res.next()) {
                throw new IllegalStateException("Too many results from " + ps);
            }
            return resultDate;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not retrieve long value from database '" 
                    + dbConnection + "' with statement '" + query + "'", e);
        }
    }
    
    /**
     * Retrieves the result-set corresponding to an unspecified object.
     * E.g. a set of several objects.
     * @param dbConnection The connection to the database.
     * @param query The SQL query to be executed on the database.
     * @param args The arguments for the SQL statement.
     * @return The requested result set.
     */
    public static ResultSet selectObject(Connection dbConnection, String query, Object... args) {
        try {
            PreparedStatement ps = createPreparedStatement(dbConnection, query, args);
            
            return ps.executeQuery();            
        } catch (SQLException e) {
            throw new IllegalStateException("Could not execute the query '" + query + "' on database '"
                    + dbConnection + "'", e);
        }        
    }
    
    /**
     * Retrieves a single String value from the database through the given query and arguments.
     * @param dbConnection The connection to the database.
     * @param query The query to extract the String value.
     * @param args The arguments for the statement.
     * @return The requested string value, or null if no such value could be found.
     */
    public static String selectStringValue(Connection dbConnection, String query, Object... args) {
        try {
            PreparedStatement ps = createPreparedStatement(dbConnection, query, args);
            
            ResultSet rs = ps.executeQuery();
            
            if(!rs.next()) {
                log.info("No string was found for the query '" + query + "'. A null has been returned.");
                return null;
            }
            
            return rs.getString(1);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not execute the query '" + query + "' on database '"
                    + dbConnection + "'", e);
        }
    }
    
    /**
     * Retrieves a list of string values from the database based on a given query and arguments.
     * 
     * @param dbConnection The connection to the database.
     * @param query The SQL query for retrieving the strings.
     * @param args The arguments for the statement.
     * @return The requested list of strings. If no strings were found, then the list is empty.
     */
    public static List<String> selectStringList(Connection dbConnection, String query, Object... args) {
        try {
            List<String> res = new ArrayList<String>();
            PreparedStatement ps = createPreparedStatement(dbConnection, query, args);
            
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                res.add(rs.getString(1));
            }
            return res;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not execute the query '" + query + "' on database '"
                    + dbConnection + "'", e);
        }
    }

    /**
     * Executing a given statement, which should not return any results.
     * This is intended to be used especially for UPDATE commands.
     * @param dbConnection The connection to the database.
     * @param query The SQL query to execute.
     * @param args The arguments for the SQL statement.
     */
    public static void executeStatement(Connection dbConnection, String query, Object... args) {
        try {
            PreparedStatement ps = createPreparedStatement(dbConnection, query, args);
            ps.executeUpdate();
            dbConnection.commit();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not execute the query '" + query + "' on database '"
                    + dbConnection + "'", e);
        }
    }
    
    /**
     * Prepare a statement given a query string and some args.
     *
     * NB: the provided connection is not closed.
     *
     * @param dbConnection The connection to the database.
     * @param query a query string  (must not be null or empty)
     * @param args some args to insert into this query string (must not be null)
     * @return a prepared statement
     * @throws SQLException If unable to prepare a statement
     */
    private static PreparedStatement createPreparedStatement(Connection dbConnection, String query, Object... args) 
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
                throw new IllegalStateException("Cannot handle type '"
                        + arg.getClass().getName()
                        + "'. We can only handle string, "
                        + "int, long, date or boolean args for query: "
                        + query);
            }
            i++;
        }
        return s;
    }
}