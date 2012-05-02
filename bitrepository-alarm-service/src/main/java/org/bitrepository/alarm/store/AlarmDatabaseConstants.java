package org.bitrepository.alarm.store;

/**
 * The constants for the AlarmService database.
 */
public class AlarmDatabaseConstants {
    /** Private constructor to prevent instantiation of this constants class.*/
    private AlarmDatabaseConstants() { }
    
    /** The name of the Alarm table.*/
    public static final String ALARM_TABLE = "alarm";
    /** The name of the guid field in the alarm table.*/
    public static final String ALARM_GUID = "guid";
    /** The name of the component guid field in the alarm table.*/
    public static final String ALARM_COMPONENT_GUID = "component_guid";
    /** The name of the alarm code field in the alarm table.*/
    public static final String ALARM_CODE = "alarm_code";
    /** The name of the alarm text field in the alarm table.*/
    public static final String ALARM_TEXT = "alarm_text";
    /** The name of the alarm date field in the alarm table.*/
    public static final String ALARM_DATE = "alarm_date";
    /** The name of the file id field in the alarm table.*/
    public static final String ALARM_FILE_ID = "file_id";
    
    /** The name of the component table.*/
    public static final String COMPONENT_TABLE = "component";
    /** The name of the component guid field in the component table.*/
    public static final String COMPONENT_GUID = "component_guid";
    /** The name of the component id field in the component table.*/
    public static final String COMPONENT_ID = "component_id";
}
