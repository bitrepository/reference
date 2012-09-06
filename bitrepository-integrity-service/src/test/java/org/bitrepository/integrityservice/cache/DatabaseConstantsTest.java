package org.bitrepository.integrityservice.cache;

import org.bitrepository.common.TestValidationUtils;
import org.bitrepository.integrityservice.cache.database.DatabaseConstants;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

public class DatabaseConstantsTest extends ExtendedTestCase {
    @Test(groups = {"regressiontest", "integritytest"})
    public void validateUtilityClass() {
        addDescription("Validating that the utility class is actually an utility class.");
        TestValidationUtils.validateUtilityClass(DatabaseConstants.class);
    }
}
