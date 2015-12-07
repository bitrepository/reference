package org.bitrepository.service.audit;

import org.bitrepository.service.database.DatabaseManager;

/**
 * AuditTrailContributerDAO which knows how to ask Derby to limit the number of results returned.
 */
public class DerbyAuditTrailContributorDAO extends AuditTrailContributerDAO {

    public DerbyAuditTrailContributorDAO(DatabaseManager manager) {
        super(manager);
    }

    @Override
    public String createQueryResultsLimit() {
        return " FETCH FIRST ? ROWS ONLY";
    }

}
