/*
 * #%L
 * Bitmagasin Protocol
 *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class for handling standard stream issues.
 */
public final class StreamUtils {
    private StreamUtils() {}
    private static final int IO_BUFFER_SIZE = 32 * 1024;

    /**
     * Utility function for moving data from an input-stream to an output-stream.
     * TODO move to a utility class.
     *
     * @param in  The input stream to copy to the output stream.
     * @param out The output stream where the input stream should be copied.
     * @throws IOException If anything problems occur with transferring the
     *                     data between the streams.
     */
    public static void copyInputStreamToOutputStream(InputStream in,
                                                     OutputStream out) throws IOException {
        if (in == null || out == null) {
            throw new IllegalArgumentException("InputStream: " + in
                    + ", OutputStream: " + out);
        }

        try (in) {
            byte[] buf = new byte[IO_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buf)) != -1) {
                out.write(buf, 0, bytesRead);
            }
            out.flush();
        }
    }

}
