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
package org.bitrepository.integrityservice.cache.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Class to handle iteration over large set of integrity issues, delivering only IDs 
 */
public class IntegrityIssueIterator implements AutoCloseable {
    
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    private ResultSet issueResultSet = null;
    private Connection conn = null;
    private final PreparedStatement ps;
    
    public IntegrityIssueIterator(PreparedStatement ps) {
        this.ps = ps;
    }
    
    /**
     * Method to explicitly close the ResultSet in the IntegrityIssueIterator 
     */
    public void close() {
        if(issueResultSet != null) {
            try {
                issueResultSet.close();
            } catch (SQLException ignored) {}
        }
        
        if(ps != null) {
            try {
                ps.close();
            } catch (SQLException ignored) {}
        }
        try {
            if (conn != null && !conn.isClosed()) {
                conn.setAutoCommit(true);
                conn.close();
            }
            conn = null;
        } catch (SQLException ignored) {}
    }
    
    /**
     * Method to return the next AuditTrailEvent in the ResultSet
     * When no more AuditTrailEvents are available, null is returned and the internal ResultSet closed. 
     * @return The next AuditTrailEvent available in the ResultSet, or null if no more events are available. 
     * @throws IllegalStateException In case of a sql error.
     * @throws RuntimeException in case the close operation failed
     */
    public String getNextIntegrityIssue() throws IllegalStateException, RuntimeException{
        try {
            String issue = null;
            if(issueResultSet == null) {
                conn = ps.getConnection();
                conn.setAutoCommit(false);
                ps.setFetchSize(100);
                long tStart = System.currentTimeMillis();
                log.debug("Executing query to get issues resultset");
                issueResultSet = ps.executeQuery();
                log.debug("Finished executing issues query, it took: " + (System.currentTimeMillis() - tStart) + "ms");
            }
            if(issueResultSet.next()) {
                issue = issueResultSet.getString(1);
            } else {
                close();
            }
    
            return issue;
        } catch (Exception e) {
            close();
            throw new IllegalStateException("Could not extract the wanted integrity issues", e);
        }
    }
}
