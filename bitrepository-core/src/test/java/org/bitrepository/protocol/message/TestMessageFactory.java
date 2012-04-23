package org.bitrepository.protocol.message;

import org.bitrepository.bitrepositorymessages.Message;

import java.math.BigInteger;

/**
 *
 */
public abstract class TestMessageFactory {
    protected static final String CORRELATION_ID_DEFAULT = "CorrelationID";
    protected static final BigInteger VERSION_DEFAULT = BigInteger.valueOf(1L);
    protected final String collectionID;

    public TestMessageFactory(String collectionID) {
        this.collectionID = collectionID;
    }

    protected void initializeMessageDetails(Message msg) {
        msg.setCollectionID(collectionID);
        msg.setVersion(VERSION_DEFAULT);
        msg.setMinVersion(VERSION_DEFAULT);
    }
}
