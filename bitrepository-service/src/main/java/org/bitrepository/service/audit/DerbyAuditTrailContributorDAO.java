package org.bitrepository.service.audit;

import org.bitrepository.service.database.DatabaseManager;

public class DerbyAuditTrailContributorDAO extends AuditTrailContributerDAO {

    public DerbyAuditTrailContributorDAO(DatabaseManager manager, String componentID) {
        super(manager, componentID);
    }

    @Override
    public String createQueryResultsLimit() {
        return " FETCH FIRST ? ROWS ONLY";
    }

}
