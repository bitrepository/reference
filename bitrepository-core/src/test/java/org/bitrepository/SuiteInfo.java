package org.bitrepository;

import org.junit.jupiter.api.TestInfo;

import java.util.Optional;

public interface SuiteInfo extends TestInfo {

    Optional<String> getPillarType();
}
