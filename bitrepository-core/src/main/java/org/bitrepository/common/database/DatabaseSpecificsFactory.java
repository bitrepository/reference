/*
 * #%L
 * Bitrepository Core
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

/**
 * Factory for instantating the specifics for a database connection.
 */
public class DatabaseSpecificsFactory {
    /**
     * Retrieves the specified class with the database connection specifics.
     * @param className The name of the database specifics.
     * @return The requested database specifics class.
     */
    @SuppressWarnings("unchecked")
    public static DBSpecifics retrieveDBSpecifics(String className) {
        try {
            Class<DBSpecifics> dbSpecs = (Class<DBSpecifics>) Class.forName(className);
            
            return (DBSpecifics) dbSpecs.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not instantiate the specifics for the database", e);
        }
    }
}
