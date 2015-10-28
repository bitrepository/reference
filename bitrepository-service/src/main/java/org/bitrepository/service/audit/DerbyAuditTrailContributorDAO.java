package org.bitrepository.service.audit;

import org.bitrepository.service.database.DatabaseManager;

public class DerbyAuditTrailContributorDAO extends AuditTrailContributerDAO {

    public DerbyAuditTrailContributorDAO(DatabaseManager manager) {
        super(manager);
    }

    @Override
    public String createQueryResultsLimit() {
        return " FETCH FIRST ? ROWS ONLY";
    }

}
