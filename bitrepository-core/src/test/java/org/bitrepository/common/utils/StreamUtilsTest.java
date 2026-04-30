/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.common.utils;

import java.io.ByteArrayInputStream;

import org.bitrepository.TestGroups;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

class StreamUtilsTest {
    String DATA = "The data for the streams.";

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void streamTester() throws Exception {
        addDescription("Tests the SteamUtils class.");
        addStep("Setup variables", "");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(DATA.getBytes(StandardCharsets.UTF_8));

        addStep("Test with null arguments", "Should throw exceptions");
        try {
            StreamUtils.copyInputStreamToOutputStream(null, out);
            Assertions.fail("Should throw an exception here.");
        } catch (Exception e) {
            Assertions.assertInstanceOf(IllegalArgumentException.class, e);
        }

        try {
            StreamUtils.copyInputStreamToOutputStream(in, null);
            Assertions.fail("Should throw an exception here.");
        } catch (Exception e) {
            Assertions.assertInstanceOf(IllegalArgumentException.class, e);
        }

        addStep("Test copying the input stream to the output stream.",
                "Should contain the same data.");
        StreamUtils.copyInputStreamToOutputStream(in, out);

        Assertions.assertEquals(DATA, out.toString(StandardCharsets.UTF_8));
    }

}
