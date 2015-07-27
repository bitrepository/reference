package org.bitrepository.alarm.store;

import org.bitrepository.service.database.DBConnector;

public class DerbyAlarmDatabaseExtractor extends AlarmDatabaseExtractor {

    public DerbyAlarmDatabaseExtractor(AlarmDatabaseExtractionModel model, DBConnector dbConnector) {
        super(model, dbConnector);
    }

    @Override
    protected String getResultNumberLimitations() {
        return " FETCH FIRST ? ROWS ONLY";
    }
    
}
