package org.bitrepository.pillar.referencepillar;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import org.bitrepository.common.database.DBConnector;
import org.bitrepository.common.database.ScriptRunner;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * Contains functionality for maintenance of the reference pillar databases. This includes functionality for
 * creating and upgrading the databases.
 *
 * Note, that all the operations found here is delegated to sql scripts, this means the sql operations
 * can be run by hand.
 */
public class ReferencePilllarDerbyDBUtils {
    public static String AUDIT_TRAIL_DB_SCRIPT = "auditContributerDB.sql";
    public static String CHECKSUM_DB_SCRIPT = "checksumDB.sql";

    /** Prevent instantiation this util class */
    private ReferencePilllarDerbyDBUtils() {}

    public static void createDatabases(Settings settings) throws Exception {
        DatabaseSpecifics auditTrailDB =
                settings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase();
        deleteDatabase(auditTrailDB);
        createDatabase(auditTrailDB, AUDIT_TRAIL_DB_SCRIPT);

        DatabaseSpecifics checksumDB =
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase();
        deleteDatabase(checksumDB);
        createDatabase(checksumDB, CHECKSUM_DB_SCRIPT);
    }

    /**
     * Attempts to find a script with the indicated name in the classpath and run it.
     * @param scriptName The name of the script in the classpath.
     */
    private static void runScript(DatabaseSpecifics databaseSpecifics, String scriptName) throws Exception {
        Connection connection = getEmbeddedDBConnection(databaseSpecifics);
        ScriptRunner scriptRunner = new ScriptRunner(connection, true, true);
        scriptRunner.runScript(getReaderForFile(scriptName));
    }

    private static Reader getReaderForFile(String filePath) throws java.io.IOException {
        return new BufferedReader(
                new InputStreamReader(ReferencePilllarDerbyDBUtils.class.getClassLoader().getResourceAsStream(filePath)));
    }

    public static DatabaseSpecifics updateDatabaseSpecificsForDBCreation(DatabaseSpecifics databaseSpecifics) {
        DatabaseSpecifics newDatabaseSpecifics = new DatabaseSpecifics();
        newDatabaseSpecifics.setDriverClass(databaseSpecifics.getDriverClass());
        newDatabaseSpecifics.setDatabaseURL(databaseSpecifics.getDatabaseURL() + ";create=true");
        newDatabaseSpecifics.setUsername(databaseSpecifics.getUsername());
        newDatabaseSpecifics.setPassword(databaseSpecifics.getPassword());
        return newDatabaseSpecifics;
    }

    private static Connection getEmbeddedDBConnection(DatabaseSpecifics databaseSpecifics) throws Exception {
        Class.forName(databaseSpecifics.getDriverClass());
        Connection connection = DriverManager.getConnection(databaseSpecifics.getDatabaseURL());
        return connection;
    }

    private static void createDatabase(DatabaseSpecifics databaseSpecifics, String scriptName) throws Exception {
    DBConnector dbConnector = new DBConnector(databaseSpecifics);

        DatabaseSpecifics databaseCreationSpecifics =
                updateDatabaseSpecificsForDBCreation(databaseSpecifics);
        runScript(databaseCreationSpecifics, scriptName);
    }

    /** Will delete the database by directly removing the files on disk based on the DB url */
    private static void deleteDatabase(DatabaseSpecifics databaseSpecifics) {
        String dbUrl = databaseSpecifics.getDatabaseURL();
        String pathToDatabase = dbUrl.substring(dbUrl.indexOf("derby:") + 6, dbUrl.length());
        FileUtils.deleteDirIfExists(new File(pathToDatabase));
    }
}
