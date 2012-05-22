package org.bitrepository.common.utils;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import org.bitrepository.common.utils.FileUtils;

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
