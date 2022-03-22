/*
 * #%L
 * Bitrepository Alarm Service
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

/**
 * The constants for the AlarmService database.
 */
public class AlarmDatabaseConstants {
    /**
     * Private constructor to prevent instantiation of this constants class.
     */
    private AlarmDatabaseConstants() {}

    /**
     * The name of the Alarm table.
     */
    public static final String ALARM_TABLE = "alarm";
    /**
     * The name of the guid field in the alarm table.
     */
    public static final String ALARM_GUID = "guid";
    /**
     * The name of the component guid field in the alarm table.
     */
    public static final String ALARM_COMPONENT_GUID = "component_guid";
    /**
     * The name of the alarm code field in the alarm table.
     */
    public static final String ALARM_CODE = "alarm_code";
    /**
     * The name of the alarm text field in the alarm table.
     */
    public static final String ALARM_TEXT = "alarm_text";
    /**
     * The name of the alarm date field in the alarm table.
     */
    public static final String ALARM_DATE = "alarm_date";
    /**
     * The name of the file id field in the alarm table.
     */
    public static final String ALARM_FILE_ID = "file_id";
    /**
     * The name of the collection id field in the alarm table.
     */
    public static final String ALARM_COLLECTION_ID = "collection_id";

    /**
     * The name of the component table.
     */
    public static final String COMPONENT_TABLE = "component";
    /**
     * The name of the component guid field in the component table.
     */
    public static final String COMPONENT_GUID = "component_guid";
    /**
     * The name of the component id field in the component table.
     */
    public static final String COMPONENT_ID = "component_id";

    /**
     * The name of the version table entry for the alarm table.
     */
    public final static String ALARM_TABLE_VERSION_ENTRY = "alarm";
    /**
     * The name of the version table entry for the database.
     */
    public final static String ALARM_DATABASE_VERSION_ENTRY = "alarmservicedb";
}
