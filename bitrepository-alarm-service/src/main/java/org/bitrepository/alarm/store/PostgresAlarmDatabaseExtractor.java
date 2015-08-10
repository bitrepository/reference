package org.bitrepository.alarm.store;

import org.bitrepository.service.database.DBConnector;

public class PostgresAlarmDatabaseExtractor extends AlarmDatabaseExtractor {

    public PostgresAlarmDatabaseExtractor(AlarmDatabaseExtractionModel model, DBConnector dbConnector) {
        super(model, dbConnector);
    }

    @Override
    protected String getResultNumberLimitations() {
        return " LIMIT ?";
    }
    
}
