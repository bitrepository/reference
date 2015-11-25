/*
 * #%L
 * Bitrepository Service
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
package org.bitrepository.service.database;

import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

public abstract class DatabaseFactory<T> {

    public static final String derbyDriver = "org.apache.derby.jdbc.EmbeddedDriver";
    public static final String postgressDriver = "org.postgresql.Driver";
    
    /**
     * Obtain the appropriate DAO instance for the concrete backend. 
     */
    protected T getDAOInstance(DatabaseSpecifics ds) {
        DatabaseManager dm = getDatabaseManager(ds);
        String dbDriver = ds.getDriverClass();
        if(dbDriver.equals(derbyDriver)) {
            return getDerbyDAO(dm);
        } else if(dbDriver.equals(postgressDriver)) {
            return getPostgresDAO(dm);
        } else {
            throw new UnsupportedDatabaseTypeException("The database for driver: '" + dbDriver
                    + "' is not supported, use '" + derbyDriver + "' or '" + postgressDriver + "'");
        }
    }
    
    protected abstract T getDerbyDAO(DatabaseManager dm);
    
    protected abstract T getPostgresDAO(DatabaseManager dm);
    
    protected abstract DatabaseManager getDatabaseManager(DatabaseSpecifics ds);
}
