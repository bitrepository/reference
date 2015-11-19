package org.bitrepository.service.audit;

import org.bitrepository.service.database.DatabaseManager;

public class PostgresAuditTrailContributorDAO extends AuditTrailContributerDAO {

    public PostgresAuditTrailContributorDAO(DatabaseManager manager) {
        super(manager);
    }

    @Override
    public String createQueryResultsLimit() {
        return " LIMIT ?";
    }

}
