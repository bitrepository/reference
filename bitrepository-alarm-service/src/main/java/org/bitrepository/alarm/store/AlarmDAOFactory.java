/*
 * #%L
 * Bitrepository Alarm Service
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
package org.bitrepository.alarm.store;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.service.database.DatabaseFactory;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * Factory class for obtaining the appropriate DAO for the specific database backend.
 */
public class AlarmDAOFactory extends DatabaseFactory<AlarmServiceDAO> {
    
    /**
     * Obtain an instance of AlarmServiceDAO appropriate for the specific database backend.
     * @param ds the specific database backend
     * @param settings
     * @return an instance of AlarmServiceDAO appropriate for the specific database backend.
     */
    public AlarmServiceDAO getAlarmServiceDAOInstance(DatabaseSpecifics ds, Settings settings) {
        AlarmServiceDAO dao = getDAOInstance(ds, settings);
        return dao;
    }

    @Override
    protected AlarmServiceDAO getDerbyDAO(DatabaseManager dm, Settings settings) {
        return new DerbyAlarmServiceDAO(dm);
    }

    @Override
    protected AlarmServiceDAO getPostgresDAO(DatabaseManager dm, Settings settings) {
        return new PostgresAlarmServiceDAO(dm);
    }

    @Override
    protected DatabaseManager getDatabaseManager(DatabaseSpecifics ds) {
        return new AlarmDatabaseManager(ds);
    }
    
}
