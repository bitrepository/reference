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
package org.apache.http.impl.conn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.entity.LaxContentLengthStrategy;
import org.apache.http.impl.entity.StrictContentLengthStrategy;
import org.apache.http.impl.io.ChunkedOutputStream;
import org.apache.http.impl.io.DefaultHttpRequestWriterFactory;
import org.apache.http.io.HttpMessageParserFactory;
import org.apache.http.io.HttpMessageWriterFactory;
import org.apache.http.io.SessionOutputBuffer;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This is a straight copy of ManagedHttpClientConnectionFactory, but it takes a chunksize param. This allows
 * you to control the size of the http chunks.
 */
public class ChunkyManagedHttpClientConnectionFactory extends ManagedHttpClientConnectionFactory {

    private static final AtomicLong COUNTER = new AtomicLong();
    private final int chunkSize;

    private final Log log = LogFactory.getLog(DefaultManagedHttpClientConnection.class);
    private final Log headerLog = LogFactory.getLog("org.apache.http.headers");
    private final Log wireLog = LogFactory.getLog("org.apache.http.wire");

    private final HttpMessageWriterFactory<HttpRequest> requestWriterFactory;
    private final HttpMessageParserFactory<HttpResponse> responseParserFactory;
    private final ContentLengthStrategy incomingContentStrategy;
    private final ContentLengthStrategy outgoingContentStrategy;


    public ChunkyManagedHttpClientConnectionFactory(final HttpMessageWriterFactory<HttpRequest> requestWriterFactory,
                                                    final HttpMessageParserFactory<HttpResponse> responseParserFactory,
                                                    final ContentLengthStrategy incomingContentStrategy,
                                                    final ContentLengthStrategy outgoingContentStrategy, int chunkSize) {
        super();
        this.chunkSize = chunkSize;
        this.requestWriterFactory = requestWriterFactory != null ? requestWriterFactory : DefaultHttpRequestWriterFactory.INSTANCE;
        this.responseParserFactory = responseParserFactory != null ? responseParserFactory : DefaultHttpResponseParserFactory.INSTANCE;
        this.incomingContentStrategy = incomingContentStrategy != null ? incomingContentStrategy : LaxContentLengthStrategy.INSTANCE;
        this.outgoingContentStrategy = outgoingContentStrategy != null ? outgoingContentStrategy : StrictContentLengthStrategy.INSTANCE;
    }


    public ChunkyManagedHttpClientConnectionFactory(final HttpMessageWriterFactory<HttpRequest> requestWriterFactory,
                                                    final HttpMessageParserFactory<HttpResponse> responseParserFactory, int chunkSize) {
        this(requestWriterFactory, responseParserFactory, null, null, chunkSize);
    }

    public ChunkyManagedHttpClientConnectionFactory(int chunkSize) {
        this(null, null, chunkSize);
    }

    @Override
    public ManagedHttpClientConnection create(HttpRoute route, ConnectionConfig config) {
        final ConnectionConfig connectionConfig = config != null ? config : ConnectionConfig.DEFAULT;
        CharsetDecoder chardecoder = null;
        CharsetEncoder charencoder = null;
        final Charset charset = connectionConfig.getCharset();
        final CodingErrorAction malformedInputAction =
                connectionConfig.getMalformedInputAction() != null ? connectionConfig.getMalformedInputAction() : CodingErrorAction.REPORT;
        final CodingErrorAction unmappableInputAction =
                connectionConfig.getUnmappableInputAction() != null ? connectionConfig.getUnmappableInputAction() :
                        CodingErrorAction.REPORT;
        if (charset != null) {
            chardecoder = charset.newDecoder();
            chardecoder.onMalformedInput(malformedInputAction);
            chardecoder.onUnmappableCharacter(unmappableInputAction);
            charencoder = charset.newEncoder();
            charencoder.onMalformedInput(malformedInputAction);
            charencoder.onUnmappableCharacter(unmappableInputAction);
        }
        final String id = "http-outgoing-" + COUNTER.getAndIncrement();
        return new ChunkyLoggingManagedHttpClientConnection(id, log, headerLog, wireLog, connectionConfig.getBufferSize(),
                connectionConfig.getFragmentSizeHint(), chardecoder, charencoder, connectionConfig.getMessageConstraints(),
                incomingContentStrategy,
                outgoingContentStrategy, requestWriterFactory, responseParserFactory, chunkSize);
    }

    static class ChunkyLoggingManagedHttpClientConnection extends LoggingManagedHttpClientConnection {
        private final int chunkSize;


        public ChunkyLoggingManagedHttpClientConnection(String id, Log log, Log headerLog, Log wireLog, int bufferSize,
                                                        int fragmentSizeHint, CharsetDecoder charDecoder, CharsetEncoder charEncoder,
                                                        MessageConstraints constraints, ContentLengthStrategy incomingContentStrategy,
                                                        ContentLengthStrategy outgoingContentStrategy,
                                                        HttpMessageWriterFactory<HttpRequest> requestWriterFactory,
                                                        HttpMessageParserFactory<HttpResponse> responseParserFactory, int chunkSize) {
            super(id, log, headerLog, wireLog, bufferSize, fragmentSizeHint, charDecoder, charEncoder, constraints, incomingContentStrategy,
                    outgoingContentStrategy, requestWriterFactory, responseParserFactory);
            this.chunkSize = chunkSize;
        }


        @Override
        protected OutputStream createOutputStream(long len, SessionOutputBuffer outBuffer) {
            if (len == ContentLengthStrategy.CHUNKED) {
                return new ChunkedOutputStream(chunkSize, outBuffer);
            }
            return super.createOutputStream(len, outBuffer);
        }
    }
}
