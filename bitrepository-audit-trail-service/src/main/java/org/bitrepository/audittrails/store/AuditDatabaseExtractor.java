/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.audittrails.store;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_NAME;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDIT_TRAIL_ACTOR_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDIT_TRAIL_AUDIT;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDIT_TRAIL_CONTRIBUTOR_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDIT_TRAIL_FILE_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDIT_TRAIL_FINGERPRINT;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDIT_TRAIL_INFORMATION;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDIT_TRAIL_OPERATION;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDIT_TRAIL_OPERATION_DATE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDIT_TRAIL_OPERATION_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDIT_TRAIL_SEQUENCE_NUMBER;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDIT_TRAIL_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_COLLECTION_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_FILE_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_TABLE;

/**
 * Extractor for the audit trail events from the AuditTrailServiceDatabase.
 * <p>
 * The actual extraction is delegated to the class AuditEventIterator.
 * As such any change in extraction model should be reflected in the AuditEventIterator.
 * For further details @see {@link org.bitrepository.audittrails.store.AuditEventIterator}
 * <p>
 * <p>
 * Order of extraction:
 * FileId, ContributorId, SequenceNumber, SeqNumber, ActorName, Operation, OperationDate,
 * AuditTrail, Information, OperationID, Certificate fingerprint
 */
public class AuditDatabaseExtractor {
    /**
     * The log.
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Position of the FileId in the extraction.
     */
    public static final int POSITION_FILE_ID = 1;
    /**
     * Position of the ContributorId in the extraction.
     */
    public static final int POSITION_CONTRIBUTOR_ID = 2;
    /**
     * Position of the SequenceNumber in the extraction.
     */
    public static final int POSITION_SEQUENCE_NUMBER = 3;
    /**
     * Position of the ActorName in the extraction.
     */
    public static final int POSITION_ACTOR_NAME = 4;
    /**
     * Position of the Operation in the extraction.
     */
    public static final int POSITION_OPERATION = 5;
    /**
     * Position of the OperationDate in the extraction.
     */
    public static final int POSITION_OPERATION_DATE = 6;
    /**
     * Position of the AuditTrail in the extraction.
     */
    public static final int POSITION_AUDIT_TRAIL = 7;
    /**
     * Position of the Information in the extraction.
     */
    public static final int POSITION_INFORMATION = 8;
    /**
     * Position of the OperationID in the extraction.
     */
    public static final int POSITION_OPERATION_ID = 9;
    /**
     * Position of the fingerprint in the extraction.
     */
    public static final int POSITION_FINGERPRINT = 10;

    /**
     * The model containing the elements for the restriction.
     */
    private final ExtractModel model;
    /**
     * The connector to the database.
     */
    private final DBConnector dbConnector;

    /**
     * Constructor.
     *
     * @param model       The model for the restriction for the extraction from the database.
     * @param dbConnector The connector to the database, where the audit trails are to be extracted.
     */
    public AuditDatabaseExtractor(ExtractModel model, DBConnector dbConnector) {
        ArgumentValidator.checkNotNull(model, "ExtractModel model");
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");

        this.model = model;
        this.dbConnector = dbConnector;
    }

    /**
     * Method to extract the requested audit trails
     *
     * @return {@link AuditEventIterator} Iterator for extracting the AuditTrails
     */
    public AuditEventIterator extractAuditEventsByIterator() {
        String sql = createSelectString() + " FROM " + AUDIT_TRAIL_TABLE + joinWithFileTable() + joinWithActorTable()
                + joinWithContributorTable() + createRestriction()
                + " ORDER BY " + AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_OPERATION_DATE;
        try {
            log.debug("Creating prepared statement with sql '" + sql + "' and arguments '"
                    + Arrays.asList(extractArgumentsFromModel()) + " for AuditEventIterator");
            PreparedStatement ps = DatabaseUtils.createPreparedStatement(dbConnector.getConnection(),
                    sql, extractArgumentsFromModel());
            return new AuditEventIterator(ps);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to retrieve the audit trails from the database", e);
        }
    }

    /**
     * NOTE: This is where the position of the constants come into play.
     * E.g. POSITION_FILE_GUID = 1 refers to the first extracted element being the AUDIT_TRAIL_FILE_GUID.
     *
     * @return Creates the SELECT string for the retrieval of the audit events.
     */
    private String createSelectString() {
        StringBuilder res = new StringBuilder();

        res.append("SELECT ");
        res.append(FILE_TABLE + "." + FILE_FILE_ID + ", ");
        res.append(CONTRIBUTOR_TABLE + "." + CONTRIBUTOR_ID + ", ");
        res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_SEQUENCE_NUMBER + ", ");
        res.append(ACTOR_TABLE + "." + ACTOR_NAME + ", ");
        res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_OPERATION + ", ");
        res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_OPERATION_DATE + ", ");
        res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_AUDIT + ", ");
        res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_INFORMATION + ", ");
        res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_OPERATION_ID + ", ");
        res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_FINGERPRINT + " ");

        return res.toString();
    }

    /**
     * Joining the AuditTrail table with the File table.
     *
     * @return The sql for joining the tables.
     */
    private String joinWithFileTable() {
        return " JOIN " + FILE_TABLE + " ON " + AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_FILE_KEY + " = " + FILE_TABLE + "."
                + FILE_KEY + " ";
    }

    /**
     * Joining the AuditTrail table with the Actor table.
     *
     * @return The sql for joining the tables.
     */
    private String joinWithActorTable() {
        return " JOIN " + ACTOR_TABLE + " ON " + AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_ACTOR_KEY + " = " + ACTOR_TABLE
                + "." + ACTOR_KEY + " ";
    }

    /**
     * Joining the AuditTrail table with the Contributor table.
     *
     * @return The sql for joining the tables.
     */
    private String joinWithContributorTable() {
        return " JOIN " + CONTRIBUTOR_TABLE + " ON " + AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_CONTRIBUTOR_KEY + " = "
                + CONTRIBUTOR_TABLE + "." + CONTRIBUTOR_KEY + " ";
    }

    /**
     * Create the restriction part of the SQL statement for extracting the requested data from the database.
     *
     * @return The restriction, or empty string if no restrictions.
     */
    private String createRestriction() {
        StringBuilder res = new StringBuilder();

        if (model.getFileId() != null) {
            nextArgument(res);
            res.append(FILE_TABLE + "." + FILE_FILE_ID + " = ? ");
        }

        if (model.getCollectionId() != null) {
            nextArgument(res);
            res.append(FILE_TABLE + "." + FILE_COLLECTION_KEY + " = ( SELECT " + COLLECTION_KEY + " FROM "
                    + COLLECTION_TABLE + " WHERE " + COLLECTION_ID + " = ? )");
        }

        if (model.getContributorId() != null) {
            nextArgument(res);
            res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_CONTRIBUTOR_KEY + " = ( SELECT " + CONTRIBUTOR_KEY
                    + " FROM " + CONTRIBUTOR_TABLE + " WHERE " + CONTRIBUTOR_ID + " = ? )");
        }

        if (model.getMinSeqNumber() != null) {
            nextArgument(res);
            res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_SEQUENCE_NUMBER + " >= ?");
        }

        if (model.getMaxSeqNumber() != null) {
            nextArgument(res);
            res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_SEQUENCE_NUMBER + " <= ?");
        }

        if (model.getActorName() != null) {
            nextArgument(res);
            res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_ACTOR_KEY + " = ( SELECT " + ACTOR_KEY + " FROM "
                    + ACTOR_TABLE + " WHERE " + ACTOR_NAME + " = ? )");
        }

        if (model.getOperation() != null) {
            nextArgument(res);
            res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_OPERATION + " = ?");
        }

        if (model.getStartDate() != null) {
            nextArgument(res);
            res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_OPERATION_DATE + " >= ?");
        }

        if (model.getEndDate() != null) {
            nextArgument(res);
            res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_OPERATION_DATE + " <= ?");
        }

        if (model.getFingerprint() != null) {
            nextArgument(res);
            res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_FINGERPRINT + " = ?");
        }

        if (model.getOperationID() != null) {
            nextArgument(res);
            res.append(AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_OPERATION_ID + " = ?");
        }

        return res.toString();
    }

    /**
     * Adds either ' AND ' or 'WHERE ' depending on whether it is the first restriction.
     *
     * @param res The StringBuilder where the restrictions are combined.
     */
    private void nextArgument(StringBuilder res) {
        if (res.length() > 0) {
            res.append(" AND ");
        } else {
            res.append(" WHERE ");
        }
    }

    /**
     * @return The list of elements in the model which are not null.
     */
    private Object[] extractArgumentsFromModel() {
        List<Object> res = new ArrayList<>();

        if (model.getFileId() != null) {
            res.add(model.getFileId());
        }

        if (model.getCollectionId() != null) {
            res.add(model.getCollectionId());
        }

        if (model.getContributorId() != null) {
            res.add(model.getContributorId());
        }

        if (model.getMinSeqNumber() != null) {
            res.add(model.getMinSeqNumber());
        }

        if (model.getMaxSeqNumber() != null) {
            res.add(model.getMaxSeqNumber());
        }

        if (model.getActorName() != null) {
            res.add(model.getActorName());
        }

        if (model.getOperation() != null) {
            res.add(model.getOperation().toString());
        }

        if (model.getStartDate() != null) {
            res.add(model.getStartDate().getTime());
        }

        if (model.getEndDate() != null) {
            res.add(model.getEndDate().getTime());
        }

        if (model.getFingerprint() != null) {
            res.add(model.getFingerprint());
        }

        if (model.getOperationID() != null) {
            res.add(model.getOperationID());
        }

        return res.toArray();
    }
}
