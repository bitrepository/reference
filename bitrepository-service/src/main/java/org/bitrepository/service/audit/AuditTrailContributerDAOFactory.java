package org.bitrepository.service.audit;

import org.bitrepository.service.database.DatabaseFactory;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

/**
 * A DatabaseFactory which can return Derby and Postgres AuditTrailContributerDAO's.
 *
 * FIXME: Code does not reflect generic type.
 */
public class AuditTrailContributerDAOFactory extends DatabaseFactory<AuditTrailContributerDAO> {

    public AuditTrailContributerDAO getAuditTrailContributorDAO(DatabaseSpecifics ds, String componentID) {
        AuditTrailContributerDAO dao = getDAOInstance(ds);
        dao.initialize(componentID);
        return dao;
    }
    
    @Override
    protected AuditTrailContributerDAO getDerbyDAO(DatabaseManager dm) {
        return new DerbyAuditTrailContributorDAO(dm);
    }

    @Override
    protected AuditTrailContributerDAO getPostgresDAO(DatabaseManager dm) {
        return new PostgresAuditTrailContributorDAO(dm);
    }

    @Override
    protected DatabaseManager getDatabaseManager(DatabaseSpecifics ds) {
        return new AuditDatabaseManager(ds);
    }
    
}
