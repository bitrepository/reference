package org.bitrepository.service.audit;

import org.bitrepository.service.database.DAO;
import org.bitrepository.service.database.DatabaseFactory;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;

public class AuditTrailContributerDAOFactory extends DatabaseFactory {

    @Override
    protected DAO getDerbyDAO(DatabaseManager dm) {
        return new DerbyAuditTrailContributorDAO(dm);
    }

    @Override
    protected DAO getPostgresDAO(DatabaseManager dm) {
        return new PostgresAuditTrailContributorDAO(dm);
    }

    @Override
    protected DatabaseManager getDatabaseManager(DatabaseSpecifics ds) {
        return new AuditDatabaseManager(ds);
    }
    
}
