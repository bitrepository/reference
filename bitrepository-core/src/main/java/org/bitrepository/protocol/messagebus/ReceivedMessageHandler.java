package org.bitrepository.protocol.messagebus;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.utils.MessageCategoryUtils;
import org.bitrepository.settings.referencesettings.MessageCategory;
import org.bitrepository.settings.referencesettings.MessageThreadPool;
import org.bitrepository.settings.referencesettings.MessageThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes care of handling the further processing by the listeners in separated thread.
 */
public class ReceivedMessageHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ExecutorModel executorModel;

    public ReceivedMessageHandler(MessageThreadPools messageThreadPools) {
        executorModel = new ExecutorModel(messageThreadPools);
    }

    /**
     * Making the handling of the message be performed in parallel.
     * @param listener The listener with should perform the actual processing of the message.
     * @param message The message to be handled by the MessageListener.
     * @param messageContext passed to the message listener.
     */
    public void deliver(MessageListener listener, Message message, MessageContext messageContext) {
        MessageProcessor processor = new MessageProcessor(listener, message, messageContext);
        executorModel.retrieveExecuter(message).execute(processor);
            }

    /**
     * Use this to close down the running executors.
     */
    public void close() {
        log.debug("Shutting down handling of received messages");
        executorModel.shutdown();
    }

    /**
     * Simple class to run the delivery of messages by the message listener.
     */
    private class MessageProcessor implements Runnable {
        /** The message listener.*/
        private final MessageListener listener;
        /** The message for the listener to handle.*/
        private final Message message;
        private final MessageContext messageContext;

        /**
         * @param listener The MessageListener to handle the message.
         * @param message The message to be handled by the MessageListener.
         * @param messageContext the message context
         */
        MessageProcessor(MessageListener listener, Message message, MessageContext messageContext) {
            this.listener = listener;
            this.message = message;
            this.messageContext = messageContext;
        }

        @Override
        public void run() {
            listener.onMessage(message, messageContext);
        }
    }

    /**
     * Contains the different executors based on collections and message types.
     */
    private class ExecutorModel {
        private CollectionExecutorModel defaultCollectionExecutorModel;
        private final Map<String, CollectionExecutorModel> collectionExecutorModelMap =
                new HashMap<String, CollectionExecutorModel>();

        /**
         * Creates the different executor services based on the supplied configuration.
         * @param messageThreadPools the thread pools of the message system
         */
        ExecutorModel(MessageThreadPools messageThreadPools) {
            if (messageThreadPools != null) {
                for (MessageThreadPool messageThreadPool : messageThreadPools.getMessageThreadPool()) {
                    List<String> collections = messageThreadPool.getCollection();
                    if (collections != null  && !collections.isEmpty()) {
                        for (String collection : collections) {
                            if (!collectionExecutorModelMap.containsKey(collection)) {
                                collectionExecutorModelMap.put(collection, new CollectionExecutorModel());
                            }
                            collectionExecutorModelMap.get(collection).addPool(messageThreadPool);
                        }
                    } else {
                        if (defaultCollectionExecutorModel == null) {
                            defaultCollectionExecutorModel = new CollectionExecutorModel();
                        }
                        defaultCollectionExecutorModel.addPool(messageThreadPool);
                    }
                }
            }

            if (defaultCollectionExecutorModel == null) {
                defaultCollectionExecutorModel = new CollectionExecutorModel();
            }
            if (defaultCollectionExecutorModel.defaultexecutor == null) {
                defaultCollectionExecutorModel.defaultexecutor = Executors.newCachedThreadPool();
            }
        }

        ExecutorService retrieveExecuter(Message message) {
            ExecutorService executor = null;
            if (message.getCollectionID() != null) {
                CollectionExecutorModel collectionExecutorModel =
                        collectionExecutorModelMap.get(message.getCollectionID());
                if (collectionExecutorModel != null) {
                    executor = collectionExecutorModel.retrieveExecuter(message);
                }
            }
            if (executor == null) {
                executor = defaultCollectionExecutorModel.retrieveExecuter(message);
            }
            return executor;
        }

        public void shutdown() {
            if (defaultCollectionExecutorModel != null) {
                defaultCollectionExecutorModel.shutdown();
            }
        }

        private ExecutorService createExecutorService(BigInteger poolSize) {
            if (poolSize == null) {
                return Executors.newCachedThreadPool();
            } else if (poolSize.intValue() == 1) {
                return Executors.newSingleThreadExecutor();
            } else {
                return Executors.newFixedThreadPool(poolSize.intValue());
            }
        }

        /**
         * Contain the executors for a single collection.
         */
        private class CollectionExecutorModel {
            private ExecutorService defaultexecutor;
            private final Map<MessageCategory, ExecutorService> categoryExecutorMap = new HashMap<MessageCategory, ExecutorService>();
            private final Map<String, ExecutorService> messageExecutorMap = new HashMap<String, ExecutorService>();

            void addPool(MessageThreadPool messageThreadPool) {
                List<String> messageNames = messageThreadPool.getMessageName();
                MessageCategory messageCategory = messageThreadPool.getMessageCategory();
                ExecutorService executor = createExecutorService(messageThreadPool.getPoolSize());
                if (messageNames != null && !messageNames.isEmpty()) {
                    for (String messageName : messageNames) {
                        messageExecutorMap.put(messageName, executor);
                    }
                } else if (messageCategory != null) {
                    categoryExecutorMap.put(messageCategory, executor);
                } else {
                    defaultexecutor = executor;
                }
            }

            ExecutorService retrieveExecuter(Message message) {
                ExecutorService executor = messageExecutorMap.get(message.getClass().getSimpleName());
                if (executor == null) {
                    MessageCategory messageCategory = MessageCategoryUtils.getCategory(message);
                    executor =  categoryExecutorMap.get(messageCategory);
                }
                if (executor == null) {
                    executor = defaultexecutor;
                }
                return executor;
            }

            void shutdown() {
                if (defaultexecutor != null) {
                    defaultexecutor.shutdown();
                }

                for (ExecutorService executor : messageExecutorMap.values()) {
                    executor.shutdown();
                }
            }
        }
    }
}
