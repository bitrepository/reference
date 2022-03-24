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
package org.bitrepository.alarm.store;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_CODE;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_COLLECTION_ID;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_COMPONENT_GUID;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_DATE;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_FILE_ID;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_TABLE;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_TEXT;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.COMPONENT_GUID;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.COMPONENT_ID;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.COMPONENT_TABLE;

/**
 * Extractor for the alarms from the AlarmServiceDatabase.
 * <p/>
 * Order of extraction:
 * ALARM_COMPONENT_GUID,
 * ALARM_CODE,
 * ALARM_TEXT,
 * ALARM_DATE,
 * ALARM_FILE_ID
 * ALARM_COLLECTION_ID
 */
public abstract class AlarmDatabaseExtractor {
    /**
     * The log.
     */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Position of the component id in the extraction.
     */
    private static final int POSITION_COMPONENT_GUID = 1;
    /**
     * Position of the alarm code in the extraction.
     */
    private static final int POSITION_ALARM_CODE = 2;
    /**
     * Position of the alarm text in the extraction.
     */
    private static final int POSITION_ALARM_TEXT = 3;
    /**
     * Position of the alarm date in the extraction.
     */
    private static final int POSITION_ALARM_DATE = 4;
    /**
     * Position of the file id in the extraction.
     */
    private static final int POSITION_FILE_ID = 5;
    /**
     * Position of the collection id in the extraction.
     */
    private static final int POSITION_COLLECTION_ID = 6;

    /**
     * The model containing the elements for the restriction.
     */
    private final AlarmDatabaseExtractionModel model;
    /**
     * The connector to the database.
     */
    private final DBConnector dbConnector;

    /**
     * Constructor.
     *
     * @param model       The model for the restriction for the extraction from the database.
     * @param dbConnector The connector to the database, where the alarms are to be extracted.
     */
    public AlarmDatabaseExtractor(AlarmDatabaseExtractionModel model, DBConnector dbConnector) {
        ArgumentValidator.checkNotNull(model, "ExtractModel model");
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");

        this.model = model;
        this.dbConnector = dbConnector;
    }

    /**
     * Extracts the requested alarms.
     *
     * @return The alarms requested through the ExtractModel.
     */
    public List<Alarm> extractAlarms() {
        String sql = createSelectString() +
                " FROM " + ALARM_TABLE +
                createRestriction() +
                createOrder() +
                getResultNumberLimitations();

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, sql, extractArgumentsFromModel())) {
            List<Alarm> res = new ArrayList<>();
            ps.setFetchSize(100);
            try (ResultSet result = ps.executeQuery()) {
                log.debug("Extracting sql '" + sql + "' with arguments '" + Arrays.asList(extractArgumentsFromModel()) + "'");
                while (result.next()) {
                    res.add(extractAlarm(result));
                }
            }
            log.debug("Extracted the alarms: {}", res);

            return res;
        } catch (Exception e) {
            throw new IllegalStateException("Could not retrieve the wanted data from the database.", e);
        }
    }

    /**
     * Extracts a single Alarm from a single result set.
     * TODO this makes several calls to be database to extract the component id. It could be reduced by joining the tables in the
     *  database in the request.
     *
     * @param resultSet The result set to extract the Alarm from.
     * @return The extracted alarm.
     * @throws SQLException if data could not be retrieved from the resultSet
     */
    private Alarm extractAlarm(ResultSet resultSet) throws SQLException {
        Alarm alarm = new Alarm();

        long componentGuid = resultSet.getLong(POSITION_COMPONENT_GUID);
        String componentId = retrieveComponentId(componentGuid);

        alarm.setAlarmCode(AlarmCode.fromValue(resultSet.getString(POSITION_ALARM_CODE)));
        alarm.setAlarmRaiser(componentId);
        alarm.setAlarmText(resultSet.getString(POSITION_ALARM_TEXT));
        alarm.setFileID(resultSet.getString(POSITION_FILE_ID));
        alarm.setOrigDateTime(CalendarUtils.getFromMillis(resultSet.getLong(POSITION_ALARM_DATE)));
        alarm.setCollectionID(resultSet.getString(POSITION_COLLECTION_ID));

        return alarm;
    }

    /**
     * NOTE: This is where the position of the constants come into play.
     * E.g. POSITION_COMPONENT_GUID = 1 refers to the first extracted element being the ALARM_COMPONENT_GUID.
     *
     * @return Creates the SELECT string for the retrieval of the audit events.
     */
    private String createSelectString() {
        return "SELECT " +
                ALARM_COMPONENT_GUID + ", " +
                ALARM_CODE + ", " +
                ALARM_TEXT + ", " +
                ALARM_DATE + ", " +
                ALARM_FILE_ID + ", " +
                ALARM_COLLECTION_ID + " ";
    }

    protected abstract String getResultNumberLimitations();

    /**
     * Create the restriction part of the SQL statement for extracting the requested data from the database.
     *
     * @return The restriction, or empty string if no restrictions.
     */
    private String createRestriction() {
        StringBuilder res = new StringBuilder();

        if (model.getComponentId() != null) {
            nextArgument(res);
            res.append(ALARM_COMPONENT_GUID + " = ( SELECT " + COMPONENT_GUID + " FROM " + COMPONENT_TABLE + " WHERE " + COMPONENT_ID +
                    " = ? )");
        }

        if (model.getAlarmCode() != null) {
            nextArgument(res);
            res.append(ALARM_CODE + " = ?");
        }

        if (model.getStartDate() != null) {
            nextArgument(res);
            res.append(ALARM_DATE + " >= ?");
        }

        if (model.getEndDate() != null) {
            nextArgument(res);
            res.append(ALARM_DATE + " <= ?");
        }

        if (model.getFileID() != null) {
            nextArgument(res);
            res.append(ALARM_FILE_ID + " = ?");
        }

        if (model.getCollectionID() != null) {
            nextArgument(res);
            res.append(ALARM_COLLECTION_ID + " = ?");
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
     * Extracts the elements from the model ordered by the respective position (see class definition).
     *
     * @return The list of elements in the model which are not null.
     */
    private Object[] extractArgumentsFromModel() {
        List<Object> res = new ArrayList<>();

        if (model.getComponentId() != null) {
            res.add(model.getComponentId());
        }

        if (model.getAlarmCode() != null) {
            res.add(model.getAlarmCode().toString());
        }

        if (model.getStartDate() != null) {
            res.add(model.getStartDate().getTime());
        }

        if (model.getEndDate() != null) {
            res.add(model.getEndDate().getTime());
        }

        if (model.getFileID() != null) {
            res.add(model.getFileID());
        }

        if (model.getCollectionID() != null) {
            res.add(model.getCollectionID());
        }

        res.add(model.getMaxCount());

        return res.toArray();
    }

    /**
     * Extracts the delivery order for the results, ascending or descending.
     *
     * @return The part of the SQL for telling which order of date the results should be delivered.
     */
    private String createOrder() {
        StringBuilder res = new StringBuilder();
        res.append(" ORDER BY " + ALARM_DATE + " ");
        if (model.getAscending()) {
            res.append("ASC");
        } else {
            res.append("DESC");
        }

        return res.toString();
    }

    /**
     * Retrieves an ID of a component based on the guid.
     *
     * @param componentGuid The guid of the component.
     * @return The id of the component corresponding to guid.
     */
    private String retrieveComponentId(long componentGuid) {
        String sqlRetrieve = "SELECT " + COMPONENT_ID + " FROM " + COMPONENT_TABLE + " WHERE " + COMPONENT_GUID + " = ?";

        return DatabaseUtils.selectStringValue(dbConnector, sqlRetrieve, componentGuid);
    }
}
