/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2015 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol.http;

import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.Args;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class to send larger chunks of data per read/write cycle.
 * InputStreamEntity sends 4k per cycle which incurs a penalty in environments with a
 * non-low latency.
 * This class amends the issue by using a larger buffer.
 */
public class LargeChunkedInputStreamEntity extends InputStreamEntity {

    private final static int OUTPUT_BUFFER_SIZE = 128 * 1024;

    public LargeChunkedInputStreamEntity(InputStream inputStream, long length) {
        super(inputStream, length);
    }

    public LargeChunkedInputStreamEntity(InputStream inputStream, ContentType contentType) {
        super(inputStream, contentType);
    }

    public LargeChunkedInputStreamEntity(InputStream inputStream, long length, ContentType contentType) {
        super(inputStream, length, contentType);
    }

    public LargeChunkedInputStreamEntity(InputStream inputStream) {
        super(inputStream);
    }

    @Override
    public void writeTo(final OutputStream outputStream) throws IOException {
        Args.notNull(outputStream, "Output stream");
        try (InputStream inputStream = getContent()) {
            final long length = getContentLength();
            final byte[] buffer = new byte[OUTPUT_BUFFER_SIZE];
            int l;
            if (length < 0) {
                // consume until EOF
                while ((l = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, l);
                }
            } else {
                // consume no more than length
                long remaining = length;
                while (remaining > 0) {
                    l = inputStream.read(buffer, 0, (int) Math.min(OUTPUT_BUFFER_SIZE, remaining));
                    if (l == -1) {
                        break;
                    }
                    outputStream.write(buffer, 0, l);
                    remaining -= l;
                }
            }
        }
    }
}
