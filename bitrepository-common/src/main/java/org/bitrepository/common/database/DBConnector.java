/*
 * #%L
 * Bitrepository Common
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
package org.bitrepository.common.database;

import java.io.File;
import java.sql.Connection;

/**
 * The interface for connecting to a database.
 */
public interface DBConnector {
    /**
     * Creates an embedded connection to a Derby database through the given URL.
     * @param dbUrl The URL to connect to the database with.
     * @return The connection to the database.
     * @throws Exception If problems with the instantiation of the connection database occurs.
     * E.g. The connection cannot be instantiated, or the driver is not available.
     */
    Connection getEmbeddedDBConnection(String dbUrl);
    
    /**
     * Instantiates the database from a SQL file.
     * @param sqlDatabaseFile The SQL file to instantiate the database from.
     */
    void createDatabase(File sqlDatabaseFile);
}
