/*
 * #%L
 * Bitrepository Audit Trail Service
 * %%
 * Copyright (C) 2010 - 2025 Royal Danish Library and The State Archives, Denmark
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
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
 * Order of extraction:
 * FileId, ContributorId, SequenceNumber, SeqNumber, ActorName, Operation, OperationDate,
 * AuditTrail, Information, OperationID, Certificate fingerprint
 */
public class AuditDatabaseExtractor {
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
     * @param model       The model for the restriction for the extraction from the database.
     * @param dbConnector The connector to the database, where the audit trails are to be extracted.
     */
    AuditDatabaseExtractor(ExtractModel model, DBConnector dbConnector) {
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
        String sql = createSelectString() +
                " FROM " + AUDIT_TRAIL_TABLE + joinWithFileTable() + joinWithActorTable() + joinWithContributorTable() +
                createRestriction() +
                " ORDER BY " + AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_OPERATION_DATE +
                createRowLimit();
        try {
            List<Object> arguments = extractArgumentsFromModel();
            log.debug("Creating prepared statement with sql '{}' and arguments '{}' for AuditEventIterator",
                    sql, arguments);
            PreparedStatement ps = DatabaseUtils.createPreparedStatement(dbConnector.getConnection(),
                    sql, arguments.toArray());
            return new AuditEventIterator(ps);
        } catch (SQLException | RuntimeException e) {
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
        return "SELECT " +
                FILE_TABLE + "." + FILE_FILE_ID + ", " +
                CONTRIBUTOR_TABLE + "." + CONTRIBUTOR_ID + ", " +
                AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_SEQUENCE_NUMBER + ", " +
                ACTOR_TABLE + "." + ACTOR_NAME + ", " +
                AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_OPERATION + ", " +
                AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_OPERATION_DATE + ", " +
                AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_AUDIT + ", " +
                AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_INFORMATION + ", " +
                AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_OPERATION_ID + ", " +
                AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_FINGERPRINT + " ";
    }

    /**
     * Joining the AuditTrail table with the File table.
     *
     * @return The sql for joining the tables.
     */
    private String joinWithFileTable() {
        return " JOIN " + FILE_TABLE +
                " ON " + AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_FILE_KEY + " = " + FILE_TABLE + "." + FILE_KEY + " ";
    }

    /**
     * Joining the AuditTrail table with the Actor table.
     *
     * @return The sql for joining the tables.
     */
    private String joinWithActorTable() {
        return " JOIN " + ACTOR_TABLE +
                " ON " + AUDIT_TRAIL_TABLE + "." + AUDIT_TRAIL_ACTOR_KEY + " = " + ACTOR_TABLE + "." + ACTOR_KEY + " ";
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

        if (model.getFileID() != null) {
            nextArgument(res);
            res.append(FILE_TABLE + "." + FILE_FILE_ID + " = ? ");
        }

        if (model.getCollectionID() != null) {
            nextArgument(res);
            res.append(FILE_TABLE + "." + FILE_COLLECTION_KEY + " = ( SELECT " + COLLECTION_KEY + " FROM "
                    + COLLECTION_TABLE + " WHERE " + COLLECTION_ID + " = ? )");
        }

        if (model.getContributorID() != null) {
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

    private String createRowLimit() {
        if (model.getMaxAuditTrails() == null) {
            return "";
        }
        return " FETCH FIRST ? ROWS ONLY";
    }

    /**
     * @return The list of elements in the model which are not null,
     * converted to types applicable for DatabaseUtils where appropriate.
     */
    private List<Object> extractArgumentsFromModel() {
        return Stream.of(model.getFileID(), model.getCollectionID(), model.getContributorID(),
                        model.getMinSeqNumber(), model.getMaxSeqNumber(), model.getActorName(),
                        model.getOperation() == null ? null : model.getOperation().toString(),
                        model.getStartDate() == null ? null : model.getStartDate().getTime(),
                        model.getEndDate() == null ? null : model.getEndDate().getTime(),
                        model.getFingerprint(), model.getOperationID(), model.getMaxAuditTrails())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

}
