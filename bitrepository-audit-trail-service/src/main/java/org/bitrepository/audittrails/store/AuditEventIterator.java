/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.audittrails.store;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.utils.CalendarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.bitrepository.audittrails.store.AuditDatabaseExtractor.*;


/**
 * Class to iterate over the set of AuditTrailEvents produced by a ResultSet.
 */
public class AuditEventIterator {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private ResultSet auditResultSet = null;
    private Connection conn = null;
    private final PreparedStatement ps;

    /**
     * @param ps The prepare statement to execute and iterate over.
     */
    public AuditEventIterator(PreparedStatement ps) {
        this.ps = ps;
    }

    /**
     * Method to explicitly close the ResultSet in the AuditEventIterator
     *
     * @throws SQLException in case of a sql error
     */
    public void close() throws SQLException {
        if (auditResultSet != null) {
            auditResultSet.close();
        }

        if (ps != null) {
            ps.close();
        }

        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
            conn = null;
        }
    }

    /**
     * Method to return the next AuditTrailEvent in the ResultSet
     * When no more AuditTrailEvents are available, null is returned and the internal ResultSet closed.
     *
     * @return The next AuditTrailEvent available in the ResultSet, or null if no more events are available.
     */
    public AuditTrailEvent getNextAuditTrailEvent() {
        try {
            AuditTrailEvent event = null;
            if (auditResultSet == null) {
                conn = ps.getConnection();
                conn.setAutoCommit(false);
                ps.setFetchSize(100);
                long tStart = System.currentTimeMillis();
                log.debug("Executing query to get AuditTrailEvents ResultSet");
                auditResultSet = ps.executeQuery();
                log.debug("Finished executing AuditTrailEvents query, it took: " + (System.currentTimeMillis() - tStart) + "ms");
            }
            if (auditResultSet.next()) {
                event = new AuditTrailEvent();
                event.setActionDateTime(CalendarUtils.getFromMillis(auditResultSet.getLong(POSITION_OPERATION_DATE)));
                event.setActionOnFile(FileAction.fromValue(auditResultSet.getString(POSITION_OPERATION)));
                event.setAuditTrailInformation(auditResultSet.getString(POSITION_AUDIT_TRAIL));
                event.setActorOnFile(auditResultSet.getString(POSITION_ACTOR_NAME));
                event.setFileID(auditResultSet.getString(POSITION_FILE_ID));
                event.setInfo(auditResultSet.getString(POSITION_INFORMATION));
                event.setReportingComponent(auditResultSet.getString(POSITION_CONTRIBUTOR_ID));
                event.setSequenceNumber(BigInteger.valueOf(auditResultSet.getLong(POSITION_SEQUENCE_NUMBER)));
                event.setOperationID(auditResultSet.getString(POSITION_OPERATION_ID));
                event.setCertificateID(auditResultSet.getString(POSITION_FINGERPRINT));
            } else {
                close();
            }

            return event;
        } catch (Exception e) {
            try {
                close();
            } catch (SQLException e1) {
                throw new RuntimeException("Failed to close ResultSet or PreparedStatement", e1);
            }
            throw new IllegalStateException("Could not extract the wanted AuditTrails", e);
        }
    }

}
