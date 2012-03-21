package org.bitrepository.integrityservice;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

/**
 * Test that collecting integrity information has the desired effect.
 */
public class IntegrityInformationCollectorTest extends ExtendedTestCase {
    @Test(groups = "specificationonly")
    void testListFileIDsCollection() throws Exception {
        addDescription("Test that requesting a file list, has the effect that the database is updated with the desired "
                               + "information");
        addStep("Set up the system", "No errors");
        addStep("Request a list of files", "Database is updated with list of files");
    }

    @Test(groups = "specificationonly")
    void testGetChecksumsCollection() throws Exception {
        addDescription("Test that requesting a set if checksums, has the effect that the database is updated with the"
                               + " desired information");
        addStep("Set up the system", "No errors");
        addStep("Request a list of checksums", "Database is updated with list of files");
    }
}
