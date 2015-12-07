package org.bitrepository.service.audit;

import org.bitrepository.service.database.DatabaseManager;

/**
 * AuditTrailContributerDAO which knows how to ask Postgres to limit the number of results returned.
 */

public class PostgresAuditTrailContributorDAO extends AuditTrailContributerDAO {

    public PostgresAuditTrailContributorDAO(DatabaseManager manager) {
        super(manager);
    }

    @Override
    public String createQueryResultsLimit() {
        return " LIMIT ?";
    }

}
