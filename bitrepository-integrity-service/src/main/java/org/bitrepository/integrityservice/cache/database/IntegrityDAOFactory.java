/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.cache.database;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.cache.IntegrityDatabaseManager;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.UnsupportedDatabaseTypeException;


/**
 * Factory class to obtain the appropriate type of DAO class for the specified database type 
 */
public class IntegrityDAOFactory {

    private static final String derbyDriver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String postgressDriver = "org.postgresql.Driver";
    
    public static IntegrityDAO getDAO2Instance(Settings settings) {
        DatabaseManager dm = new IntegrityDatabaseManager(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        String dbDriver = settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase().getDriverClass(); 
        if(dbDriver.equals(derbyDriver)) {
            return new DerbyIntegrityDAO(dm.getConnector());    
        } else if(dbDriver.equals(postgressDriver)) {
            return new PostgresIntegrityDAO(dm.getConnector());
        } else {
            throw new UnsupportedDatabaseTypeException("The database for driver: '" + dbDriver
                    + "' is not supported, use '" + derbyDriver + "' or '" + postgressDriver + "'");
        }
    }
    
}
