/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2026 Royal Danish Library and The State Archives, Denmark
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
package org.bitrepository.protocol;

import org.bitrepository.TestGroups;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OperationTypeTest {
    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void fromValueRoundTripsEveryOperationTypeTest() {
        for (OperationType operationType : OperationType.values()) {
            assertEquals(operationType, OperationType.fromValue(operationType.value()));
        }
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void fromValueUnknownValueTest() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> OperationType.fromValue("NoSuchOperation"));
        assertEquals("NoSuchOperation", e.getMessage());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void fromValueIsCaseSensitiveTest() {
        assertThrows(IllegalArgumentException.class, () -> OperationType.fromValue("getfile"));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void fromValueNullTest() {
        assertThrows(IllegalArgumentException.class, () -> OperationType.fromValue(null));
    }
}
