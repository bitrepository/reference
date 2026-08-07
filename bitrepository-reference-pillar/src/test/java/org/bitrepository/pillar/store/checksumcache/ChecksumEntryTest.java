package org.bitrepository.pillar.store.checksumcache;
/*
 * #%L
 * Bitrepository Reference Pillar
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import org.bitrepository.pillar.store.checksumdatabase.ChecksumEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

class ChecksumEntryTest {
    private static final String CE_FILE = "file";
    private static final String CE_CHECKSUM = "checksum";
    private static final Instant CE_DATE = Instant.ofEpochMilli(1234567890);

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testExtendedTestCase() {
        addDescription("Test the ChecksumEntry");
        addStep("Create a ChecksumEntry", "The data should be extractable again.");
        ChecksumEntry ce = new ChecksumEntry(CE_FILE, CE_CHECKSUM, CE_DATE);
        Assertions.assertEquals(CE_FILE, ce.getFileId());
        Assertions.assertEquals(CE_CHECKSUM, ce.getChecksum());
        Assertions.assertEquals(CE_DATE, ce.getCalculationInstant());
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void compactConstructorRejectsNullFileID() {
        addDescription("The compact constructor must reject a null fileID");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ChecksumEntry(null, CE_CHECKSUM, CE_DATE));
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void compactConstructorRejectsEmptyFileID() {
        addDescription("The compact constructor must reject an empty fileID");
        Assertions.assertThrows(IllegalArgumentException.class, () -> new ChecksumEntry("", CE_CHECKSUM, CE_DATE));
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void equalityIsComponentBased() {
        addDescription("Two ChecksumEntries with identical components must be equal");
        ChecksumEntry a = new ChecksumEntry(CE_FILE, CE_CHECKSUM, CE_DATE);
        ChecksumEntry b = new ChecksumEntry(CE_FILE, CE_CHECKSUM, CE_DATE);
        Assertions.assertEquals(a, b);
        Assertions.assertEquals(a.hashCode(), b.hashCode());
    }
}
