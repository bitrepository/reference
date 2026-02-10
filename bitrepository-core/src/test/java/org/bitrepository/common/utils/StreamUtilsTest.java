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

import org.apache.activemq.util.ByteArrayInputStream;
import org.jaccept.structure.ExtendedTestCase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.bitrepository.common.utils.StreamUtils.copyInputStreamToOutputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

public class StreamUtilsTest extends ExtendedTestCase {
    String DATA = "The data for the streams.";

    @Test
    @Tag("regressiontest")
    public void streamTester() throws Exception {
        addDescription("Tests the SteamUtils class.");
        addStep("Setup variables", "");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(DATA.getBytes(UTF_8));

        addStep("Test with null arguments", "Should throw exceptions");
        try {
            copyInputStreamToOutputStream(null, out);
            fail("Should throw an exception here.");
        } catch (Exception e) {
            assertInstanceOf(IllegalArgumentException.class, e);
        }

        try {
            copyInputStreamToOutputStream(in, null);
            fail("Should throw an exception here.");
        } catch (Exception e) {
            assertInstanceOf(IllegalArgumentException.class, e);
        }

        addStep("Test copying the input stream to the output stream.", "Should contain the same data.");
        copyInputStreamToOutputStream(in, out);

        assertEquals(DATA, out.toString(UTF_8));
    }

}
