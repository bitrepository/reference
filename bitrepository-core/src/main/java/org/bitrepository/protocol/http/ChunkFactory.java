package org.bitrepository.protocol.http;

import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.ContentLengthStrategy;
import org.apache.http.impl.conn.DefaultManagedHttpClientConnection;
import org.apache.http.impl.conn.ManagedHttpClientConnectionFactory;
import org.apache.http.impl.io.ChunkedOutputStream;
import org.apache.http.io.SessionOutputBuffer;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicLong;

public class ChunkFactory {

    public static class CustomManagedHttpClientConnection extends DefaultManagedHttpClientConnection {
        private final int chunkSize;

        public CustomManagedHttpClientConnection(final String id, final int buffersize, final int chunkSize) {
            super(id, buffersize);
            this.chunkSize = chunkSize;
        }

        @Override
        protected OutputStream createOutputStream(long len, SessionOutputBuffer outbuffer) {
            if (len == ContentLengthStrategy.CHUNKED) {
                return new ChunkedOutputStream(chunkSize, outbuffer);
            }
            return super.createOutputStream(len, outbuffer);
        }
    }

    public static class CustomManagedHttpClientConnectionFactory extends ManagedHttpClientConnectionFactory {

        private static final AtomicLong COUNTER = new AtomicLong();
        private final int chunkSize;

        public CustomManagedHttpClientConnectionFactory(int chunkSize) {
            this.chunkSize = chunkSize;
        }

        @Override
        public ManagedHttpClientConnection create(HttpRoute route, ConnectionConfig config) {
            final String id = "http-outgoing-" + Long.toString(COUNTER.getAndIncrement());
            return new CustomManagedHttpClientConnection(id, config.getBufferSize(), chunkSize);
        }
    }
}