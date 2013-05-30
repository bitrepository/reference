/*
 * #%L
 * Bitrepository Service
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class for migrating the databases.
 * Handles the default operations.
 * 
 * It is asserted, that the database has the following table for the versions:
 *  
 * create table tableversions (
 *   tablename varchar(100) not null, -- Name of table
 *   version int not null             -- version of table
 * );
 */
public abstract class DatabaseMigrator {
    /** The connection to the database.*/
    protected final DBConnector connector;
    
    /** The name of the "table versions" table.*/
    protected static final String TABLEVERSIONS_TABLE = "tableversions";
    /** The 'tablename' coloumn in the table.*/
    protected static final String TV_TABLENAME = "tablename";
    /** The 'version' coloumn in the table.*/
    protected static final String TV_VERSION = "version";
    
    /**
     * Constructor.
     * @param connector The connector for the database.
     */
    protected DatabaseMigrator(DBConnector connector) {
        this.connector = connector;
    }
    
    /**
     * Extracts the version numbers for the tables in the database.
     * @return The mapping between the table names and their respective version number.
     */
    protected Map<String, Integer> getTableVersions() {
        Map<String, Integer> resultMap = new HashMap<String, Integer>();
        
        // Extract the table name as first coloumn and version as second coloumn. 
        String sql = "SELECT " + TV_TABLENAME + " , " + TV_VERSION + " FROM " + TABLEVERSIONS_TABLE;
        int tablenameColoumn = 1;
        int versionColoumn = 2;
        
        try {
            PreparedStatement ps = null;
            ResultSet res = null;
            Connection conn = null;
            try {
                conn = connector.getConnection();
                ps = DatabaseUtils.createPreparedStatement(conn, sql, new Object[0]);
                res = ps.executeQuery();
                
                while (res.next()) {
                    resultMap.put(res.getString(tablenameColoumn), res.getInt(versionColoumn));
                }
            } finally {
                if(res != null) {
                    res.close();
                }
                if(ps != null) {
                    ps.close();
                }
                if(conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Cannot extract the table versions.", e);
        }
        
        return resultMap;
    }
    
    /**
     * Performs the update of a single table.
     * @param tablename The name of the table to update.
     * @param newVersion The new version for the table.
     * @param updateSql The SQL for updating the table.
     * @param args The arguments for performing this update.
     */
    protected void updateTable(String tablename, Integer newVersion, String updateSql, Object ... args) {
        String migrateSql = "UPDATE " + TABLEVERSIONS_TABLE + " SET " + TV_VERSION + " = ? WHERE " + TV_TABLENAME 
                + " = ?";
        
        DatabaseUtils.executeStatement(connector, updateSql, args);
        DatabaseUtils.executeStatement(connector, migrateSql, newVersion, tablename);
    }
    
    /**
     * Perform the migration for the given database.
     */
    abstract public void migrate();
}
