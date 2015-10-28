package org.bitrepository.service.audit;

import org.bitrepository.service.database.UnsupportedDatabaseTypeException;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

public class AuditTrailContributerDAOFactory {

    private static final String derbyDriver = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String postgressDriver = "org.postgresql.Driver";
    
    /**
     * Obtain an instance of AlarmServiceDAO appropriate for the specific database backend. 
     */
    public static AuditTrailContributerDAO getAuditTrailContributerDAOInstance(DatabaseSpecifics ds, String componentID) {
        AuditDatabaseManager dm = new AuditDatabaseManager(ds);
        String dbDriver = ds.getDriverClass();
        if(dbDriver.equals(derbyDriver)) {
            return new DerbyAuditTrailContributorDAO(dm, componentID);
        } else if(dbDriver.equals(postgressDriver)) {
            return new PostgresAuditTrailContributorDAO(dm, componentID);
        } else {
            throw new UnsupportedDatabaseTypeException("The database for driver: '" + dbDriver
                    + "' is not supported, use '" + derbyDriver + "' or '" + postgressDriver + "'");
        }
    }
    
}
