package org.bitrepository.alarm.store;

import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.service.database.DatabaseManager;

public class PostgresAlarmServiceDAO extends AlarmServiceDAO {

    public PostgresAlarmServiceDAO(DatabaseManager databaseManager) {
        super(databaseManager);
    }

    @Override
    public List<Alarm> extractAlarms(String componentID, AlarmCode alarmCode, Date minDate, Date maxDate, 
            String fileID, String collectionID, Integer count, boolean ascending)  {
        AlarmDatabaseExtractionModel extractModel = new AlarmDatabaseExtractionModel(collectionID, componentID, alarmCode, 
                minDate, maxDate, fileID, count, ascending);

        AlarmDatabaseExtractor extractor = new PostgresAlarmDatabaseExtractor(extractModel, dbConnector);
        return extractor.extractAlarms();
    }

}
