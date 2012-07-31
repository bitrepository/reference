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
package org.bitrepository.common.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseTestUtils {
    /** Get access to the database stored in the given file.  This will start
     * a new transaction that will be rolled back with dropDatabase.
     * Only one connection can be taken at a time.
     *
     * @param jarfile A file that contains a test database.
     * @param dbUnzipDir
     * @return a connection to the database stored in the given file
     * @throws Exception If anything goes wrong.
     */
    public static Connection takeDatabase(File jarfile, String dbname, File dbUnzipDir)
            throws Exception {

        FileUtils.delete(new File(dbUnzipDir, dbname));
        FileUtils.unzip(jarfile, dbUnzipDir);

        final String dbfile = dbUnzipDir + "/" + dbname;

        /* Set DB name */
        String driverName = "org.apache.derby.jdbc.EmbeddedDriver";
        Class.forName(driverName).newInstance();
            
        String dburi = "jdbc:derby:" + dbfile;
        return DriverManager.getConnection(dburi);
    }
}