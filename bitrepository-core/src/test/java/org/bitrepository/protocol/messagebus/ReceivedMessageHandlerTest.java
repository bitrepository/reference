package org.bitrepository.protocol.messagebus;

import java.math.BigInteger;
import java.util.Arrays;

import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.settings.referencesettings.MessageCategory;
import org.bitrepository.settings.referencesettings.MessageThreadPool;
import org.bitrepository.settings.referencesettings.MessageThreadPools;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class ReceivedMessageHandlerTest extends ExtendedTestCase {

    @Test(groups = { "regressiontest" })
    public void singleMessageDispatch() {
        addDescription("Tests that a single message is dispatched correctly");
        ReceivedMessageHandler handler = new ReceivedMessageHandler(null);
        MessageListener defaultListener = mock(MessageListener.class);
        Message testMessage = new Message();
        MessageContext testMessageContext = new MessageContext("fingerprint");
        handler.deliver(defaultListener, testMessage,  testMessageContext);
        verify(defaultListener, timeout(100)).onMessage(testMessage, testMessageContext);
    }

    @Test(groups = { "regressiontest" })
    public void parallelMessageDispatch() {
        addDescription("Tests that two messages can be handled in parallel in the default pool configuration.");
        addFixture("Create a ReceivedMessageHandler with a null configuration. This should create a " +
                "ReceivedMessageHandler with a single CachedThreadPool.");
        ReceivedMessageHandler handler = new ReceivedMessageHandler(createMessageThreadPools(
                createMessageThreadPool(2, null, null, null)
        ));

        addStep("Dispatch messages to two listeners, the first blocking.", "The second listener should be notified");
        BlockingMessageListener blockingListener = new BlockingMessageListener(mock(MessageListener.class));
        MessageListener secondListener = mock(MessageListener.class);
        Message testMessage = new Message();
        deliverAsynchronously(handler, testMessage, blockingListener);
        deliverAsynchronously(handler, testMessage, secondListener);
        verifyNoMoreInteractions(blockingListener.listener);
        verify(secondListener).onMessage(testMessage, null);

        addStep("Unblock the first listener", "The blocking listener should now be notified.");
        blockingListener.unblock();
        verify(blockingListener.listener, timeout(100)).onMessage(testMessage, null);
        verifyNoMoreInteractions(secondListener);
    }

    @Test(groups = { "regressiontest" })
    public void manyMessageDispatch() {
        addDescription("Tests that many (50) messages can be handled in parallel in the default pool configuration.");
        addFixture("Create a ReceivedMessageHandler with a null configuration. This should create a " +
                "ReceivedMessageHandler with a single CachedThreadPool.");
        ReceivedMessageHandler handler = new ReceivedMessageHandler(null);

        addStep("Dispatch messages to 49 listeners, where the first 49 are blocking.",
                "The 50'th listener should be notified");
        BlockingMessageListener[] blockingListeners = createBlockingMessageListeners(49);
        MessageListener lastListener = mock(MessageListener.class);
        Message testMessage = new Message();
        deliverAsynchronously(handler, testMessage, blockingListeners);
        deliverAsynchronously(handler, testMessage, lastListener);

        for (BlockingMessageListener blockingListener : blockingListeners) {
            verifyNoMoreInteractions(blockingListener.listener);
        }
        verify(lastListener, timeout(100)).onMessage(testMessage, null);

        addStep("Unblock the blocked listeners", "The remaining listener should now be notified.");
        for (BlockingMessageListener blockingListener : blockingListeners) {
            blockingListener.unblock();
            verify(blockingListener.listener, timeout(100)).onMessage(testMessage, null);
        }
        verifyNoMoreInteractions(lastListener);
    }

    @Test(groups = { "regressiontest" })
    public void singleThreadMessageDispatch() {
        addDescription("Tests that two messages will be handled in sequence if a singleThreaded pool is configured.");
        addFixture("Create a ReceivedMessageHandler with a single pool of size 1.");
        ReceivedMessageHandler handler = new ReceivedMessageHandler(createMessageThreadPools(
                createMessageThreadPool(1, null, null, null)
        ));

        addStep("Dispatch messages to two listeners, the first blocking.", "The second listener should be not be notified");
        BlockingMessageListener blockingListener = new BlockingMessageListener(mock(MessageListener.class));
        MessageListener secondListener = mock(MessageListener.class);
        Message testMessage = new Message();
        deliverAsynchronously(handler, testMessage, blockingListener, secondListener);
        verifyNoMoreInteractions(blockingListener.listener, secondListener);

        addStep("Unblock the first listener", "Both listeners should now be notified.");
        blockingListener.unblock();
        verify(blockingListener.listener, timeout(100)).onMessage(testMessage, null);
        verify(secondListener, timeout(100)).onMessage(testMessage, null);
    }

    @Test(groups = { "regressiontest" })
    public void specificMessagePools() {
        addDescription("Tests that different message types can be handled by different executors.");
        addFixture("Create a ReceivedMessageHandler with a two pools, one for status requests and one for put requests. " +
                "The put file pool should be limited to 1 thread.");
        String[] statusRequestFilter = new String [] {GetStatusRequest.class.getSimpleName()};
        String[] putRequestFilter = new String [] {PutFileRequest.class.getSimpleName()};
        ReceivedMessageHandler handler = new ReceivedMessageHandler(createMessageThreadPools(
                createMessageThreadPool(1, null, null, putRequestFilter),
                createMessageThreadPool(null, null, null, statusRequestFilter)
        ));

        addStep("Dispatch two put messages, blocking on the processing of the first message.",
                "None of the messages should be processed.");
        BlockingMessageListener blockingPutListener = new BlockingMessageListener(mock(MessageListener.class));
        MessageListener secondPutListener = mock(MessageListener.class);
        MessageListener thirdStatusListener = mock(MessageListener.class);
        Message getStatusRequest = new GetStatusRequest();
        Message putFileRequest = new PutFileRequest();
        deliverAsynchronously(handler, putFileRequest, blockingPutListener, secondPutListener);
        verifyNoMoreInteractions(blockingPutListener.listener, secondPutListener);

        addStep("Dispatch a status request message.",
                "This should be processed.");
        deliverAsynchronously(handler, getStatusRequest, thirdStatusListener);
        verify(thirdStatusListener, timeout(100)).onMessage(getStatusRequest, null);

        addStep("Unblock the blocked put listener", "Both put messages should now be processed.");
        blockingPutListener.unblock();
        verify(blockingPutListener.listener, timeout(100)).onMessage(putFileRequest, null);
        verify(secondPutListener, timeout(100)).onMessage(putFileRequest, null);
        verifyNoMoreInteractions(thirdStatusListener);
    }

    @Test(groups = { "regressiontest" })
    public void specificMessageNamePoolAndDefaultPool() {
        addDescription("Tests it is possible to specify a pool for a specific message type, with a " +
                "default pool for the remainder.");
        addFixture("Create a ReceivedMessageHandler with a one specific pool for put requests. " +
                "The put file pool should be limited to 1 thread.");
        String[] putRequestFilter = new String [] {PutFileRequest.class.getSimpleName()};
        ReceivedMessageHandler handler = new ReceivedMessageHandler(createMessageThreadPools(
                createMessageThreadPool(1, null, null, putRequestFilter)
        ));

        addStep("Dispatch two put messages, blocking on the processing of the first message.",
                "None of the messages should be processed.");
        BlockingMessageListener blockingPutListener = new BlockingMessageListener(mock(MessageListener.class));
        MessageListener secondPutListener = mock(MessageListener.class);
        MessageListener thirdStatusListener = mock(MessageListener.class);
        Message getStatusRequest = new GetStatusRequest();
        Message putFileRequest = new PutFileRequest();
        deliverAsynchronously(handler, putFileRequest, blockingPutListener, secondPutListener);
        verifyNoMoreInteractions(blockingPutListener.listener, secondPutListener);

        addStep("Dispatch a status request message.",
                "This should be processed.");
        deliverAsynchronously(handler, getStatusRequest, thirdStatusListener);
        verify(thirdStatusListener, timeout(100)).onMessage(getStatusRequest, null);

        addStep("Unblock the blocked put listener", "Both put messages should now be processed.");
        blockingPutListener.unblock();
        verify(blockingPutListener.listener, timeout(100)).onMessage(putFileRequest, null);
        verify(secondPutListener, timeout(100)).onMessage(putFileRequest, null);
        verifyNoMoreInteractions(thirdStatusListener);
    }

    @Test(groups = { "regressiontest" })
    public void specificMessageCategoryPoolAndDefaultPool() {
        addDescription("Tests it is possible to specify a pool for a specific message category, with a " +
                "default pool for the remainder.");
        addFixture("Create a ReceivedMessageHandler with a one specific pool for slow requests. " +
                "The put file pool should be limited to 1 thread.");
        ReceivedMessageHandler handler = new ReceivedMessageHandler(createMessageThreadPools(
                createMessageThreadPool(1, null, MessageCategory.SLOW, null)
        ));

        addStep("Dispatch two put messages, blocking on the processing of the first message.",
                "None of the messages should be processed.");
        BlockingMessageListener blockingPutListener = new BlockingMessageListener(mock(MessageListener.class));
        MessageListener secondPutListener = mock(MessageListener.class);
        MessageListener thirdStatusListener = mock(MessageListener.class);
        Message getStatusRequest = new GetStatusRequest();
        Message putFileRequest = new PutFileRequest();
        deliverAsynchronously(handler, putFileRequest, blockingPutListener, secondPutListener);
        verifyNoMoreInteractions(blockingPutListener.listener, secondPutListener);

        addStep("Dispatch a status request message.",
                "This should be processed.");
        deliverAsynchronously(handler, getStatusRequest, thirdStatusListener);
        verify(thirdStatusListener, timeout(100)).onMessage(getStatusRequest, null);

        addStep("Unblock the blocked put listener", "Both put messages should now be processed.");
        blockingPutListener.unblock();
        verify(blockingPutListener.listener, timeout(100)).onMessage(putFileRequest, null);
        verify(secondPutListener, timeout(100)).onMessage(putFileRequest, null);
        verifyNoMoreInteractions(thirdStatusListener);
    }

    @Test(groups = { "regressiontest" })
    public void specificCollectionPoolAndDefaultPool() {
        addDescription("Tests it is possible to specify a pool for a specific collection, with a " +
                "default pool for the remainder.");
        addFixture("Create a ReceivedMessageHandler with a one specific pool for Collection1. " +
                "The Collection1 pool should be limited to 1 thread.");
        String collection1 = "Collection1";
        ReceivedMessageHandler handler = new ReceivedMessageHandler(createMessageThreadPools(
                createMessageThreadPool(1, new String[] {collection1}, null, null)
        ));

        addStep("Dispatch two messages for collection1, blocking on the processing of the first message.",
                "None of the messages should be processed.");
        BlockingMessageListener blockingCollection1Listener = new BlockingMessageListener(mock(MessageListener.class));
        MessageListener secondCollection1Listener = mock(MessageListener.class);
        MessageListener thirdListener = mock(MessageListener.class);
        Message collection1Message = new Message();
        collection1Message.setCollectionID(collection1);
        deliverAsynchronously(handler, collection1Message, blockingCollection1Listener, secondCollection1Listener);
        verifyNoMoreInteractions(blockingCollection1Listener.listener, secondCollection1Listener);

        addStep("Dispatch a message for the other collection.",
                "This should be processed.");
        Message otherCollectionMessage = new Message();
        otherCollectionMessage.setCollectionID("OtherCollection");
        deliverAsynchronously(handler, otherCollectionMessage, thirdListener);
        verify(thirdListener, timeout(100)).onMessage(otherCollectionMessage, null);

        addStep("Dispatch a message with no collectionID.",
                "This should be processed.");
        Message noCollectionMessage = new Message();
        deliverAsynchronously(handler, noCollectionMessage, thirdListener);
        verify(thirdListener, timeout(100)).onMessage(noCollectionMessage, null);

        addStep("Unblock the blocked collection1 listener", "Both collection1 messages should now be processed.");
        blockingCollection1Listener.unblock();
        verify(blockingCollection1Listener.listener, timeout(100)).onMessage(collection1Message, null);
        verify(secondCollection1Listener, timeout(100)).onMessage(collection1Message, null);
    }

    @Test(groups = { "regressiontest" })
    public void specificCollectionPoolWithSpecificMessageTypePool() {
        addDescription("Tests it is possible to specify a pool for a specific collection for only a specific" +
                "message type.");
        addFixture("Create a ReceivedMessageHandler with a one specific pool for Collection1 and PutFileRequests. " +
                "The pool should be limited to 1 thread.");
        String collection1 = "Collection1";
        String[] putRequestFilter = new String [] {PutFileRequest.class.getSimpleName()};
        ReceivedMessageHandler handler = new ReceivedMessageHandler(createMessageThreadPools(
                createMessageThreadPool(1, new String[] {collection1}, null, putRequestFilter)
        ));

        addStep("Dispatch two putFileRequests for collection1, blocking on the processing of the first message.",
                "None of the messages should be processed.");
        BlockingMessageListener blockingCollection1Listener = new BlockingMessageListener(mock(MessageListener.class));
        MessageListener secondCollection1Listener = mock(MessageListener.class);
        MessageListener thirdListener = mock(MessageListener.class);
        Message putFileRequest = new PutFileRequest();
        putFileRequest.setCollectionID(collection1);
        deliverAsynchronously(handler, putFileRequest, blockingCollection1Listener, secondCollection1Listener);
        verifyNoMoreInteractions(blockingCollection1Listener.listener, secondCollection1Listener);

        addStep("Dispatch a putFile request for the other collection.",
                "This should be processed.");
        Message otherCollectionRequest = new PutFileRequest();
        otherCollectionRequest.setCollectionID("OtherCollection");
        deliverAsynchronously(handler, otherCollectionRequest, thirdListener);
        verify(thirdListener, timeout(100)).onMessage(otherCollectionRequest, null);

        addStep("Dispatch a status request for collection1.",
                "This should be processed.");
        Message getStatusRequest = new GetStatusRequest();
        deliverAsynchronously(handler, getStatusRequest, thirdListener);
        verify(thirdListener, timeout(100)).onMessage(getStatusRequest, null);

        addStep("Unblock the blocked collection1 listener", "Both collection1 putFileRequests should now be processed.");
        blockingCollection1Listener.unblock();
        verify(blockingCollection1Listener.listener, timeout(100)).onMessage(putFileRequest, null);
        verify(secondCollection1Listener, timeout(100)).onMessage(putFileRequest, null);
    }

    private BlockingMessageListener[] createBlockingMessageListeners(int number) {
        BlockingMessageListener[] listeners = new BlockingMessageListener[number];
        for (int i=0; i<number;i++) {
            listeners[i] = new BlockingMessageListener(mock(MessageListener.class));
        }
        return listeners;
    }

    private void deliverAsynchronously(
            final ReceivedMessageHandler handler, final Message message, final MessageListener... listeners) {
        for (final MessageListener listener : listeners) {
                    handler.deliver(listener, message, null);
        }
        //Ensure the messages have time to be distributed before the new step.
        try { Thread.sleep(100); } catch (InterruptedException e) {}
    }


    private class BlockingMessageListener implements MessageListener {
        final MessageListener listener;

        private BlockingMessageListener(MessageListener listener) {
            this.listener = listener;
        }

        @Override
        public synchronized void onMessage(Message message, MessageContext messageContext) {
            try {
                wait(5000);
            } catch (InterruptedException e) {}
            listener.onMessage(message, messageContext);
        }

        public synchronized void unblock() {
            notify();
        }
    }

    private MessageThreadPools createMessageThreadPools(MessageThreadPool... poolConfigurations) {
        MessageThreadPools poolsConfigurations = new MessageThreadPools();
        poolsConfigurations.getMessageThreadPool().addAll(Arrays.asList(poolConfigurations));
        return poolsConfigurations;
    }

    private MessageThreadPool createMessageThreadPool(Integer poolSize,
                                                      String[] collections, MessageCategory messageCategory,String[] messageNames) {
        MessageThreadPool poolConfiguration = new MessageThreadPool();
        if (poolSize != null) {
            poolConfiguration.setPoolSize(BigInteger.valueOf(poolSize));
        }
        if (collections != null) {
            poolConfiguration.getCollection().addAll(Arrays.asList(collections));
        }
        if (messageCategory != null) {
            poolConfiguration.setMessageCategory(messageCategory);
        }

        if (messageNames != null) {
            poolConfiguration.getMessageName().addAll(Arrays.asList(messageNames));
        }
        return poolConfiguration;
    }
}
