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
package org.bitrepository.protocol.messagebus;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.utils.MessageCategoryUtils;
import org.bitrepository.settings.referencesettings.MessageCategory;
import org.bitrepository.settings.referencesettings.MessageThreadPool;
import org.bitrepository.settings.referencesettings.MessageThreadPools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Takes care of handling the further processing by the listeners in separated thread.
 */
public class ReceivedMessageHandler implements Closeable {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ExecutorModel executorModel;
    /** Builds the virtual threads backing the bounded-concurrency executors below. */
    private final ThreadFactory threadFactory = Thread.ofVirtual()
            .name("ReceivedMessageHandler-", 0)
            .uncaughtExceptionHandler((thread, throwable) -> {
                String throwingClass = throwable.getStackTrace()[0].getClassName();
                Logger logger = LoggerFactory.getLogger(throwingClass);
                logger.error("UncaughtExceptionHandler caught Exception:", throwable);
            })
            .factory();

    public ReceivedMessageHandler(MessageThreadPools messageThreadPools) {
        executorModel = new ExecutorModel(messageThreadPools);
    }

    /**
     * Making the handling of the message be performed in parallel.
     *
     * @param listener       The listener with should perform the actual processing of the message.
     * @param message        The message to be handled by the MessageListener.
     * @param messageContext passed to the message listener.
     */
    public void deliver(MessageListener listener, Message message, MessageContext messageContext) {
        MessageProcessor processor = new MessageProcessor(listener, message, messageContext);
        executorModel.retrieveExecutor(message).execute(processor);
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
    private static class MessageProcessor implements Runnable {
        /**
         * The message listener.
         */
        private final MessageListener listener;
        /**
         * The message for the listener to handle.
         */
        private final Message message;
        private final MessageContext messageContext;

        /**
         * @param listener       The MessageListener to handle the message.
         * @param message        The message to be handled by the MessageListener.
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

    private static final class ConcurrencyLimitedExecutorService extends AbstractExecutorService {
        private final ExecutorService delegate;
        private final Semaphore concurrencyLimiter;

        ConcurrencyLimitedExecutorService(int maxConcurrency, ThreadFactory virtualThreadFactory) {
            delegate = Executors.newThreadPerTaskExecutor(virtualThreadFactory);
            concurrencyLimiter = new Semaphore(maxConcurrency);
        }

        @Override
        public void execute(Runnable task) {
            delegate.execute(() -> {
                concurrencyLimiter.acquireUninterruptibly();
                try {
                    task.run();
                } finally {
                    concurrencyLimiter.release();
                }
            });
        }

        @Override
        public void shutdown() {
            delegate.shutdown();
        }

        @Override
        public List<Runnable> shutdownNow() {
            return delegate.shutdownNow();
        }

        @Override
        public boolean isShutdown() {
            return delegate.isShutdown();
        }

        @Override
        public boolean isTerminated() {
            return delegate.isTerminated();
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return delegate.awaitTermination(timeout, unit);
        }
    }

    /**
     * Contains the different executors based on collections and message types.
     */
    private class ExecutorModel {
        private CollectionExecutorModel defaultCollectionExecutorModel;
        private final Map<String, CollectionExecutorModel> collectionExecutorModelMap =
                new HashMap<>();

        /**
         * Creates the different executor services based on the supplied configuration.
         *
         * @param messageThreadPools the thread pools of the message system
         */
        ExecutorModel(MessageThreadPools messageThreadPools) {
            if (messageThreadPools != null) {
                for (MessageThreadPool messageThreadPool : messageThreadPools.getMessageThreadPool()) {
                    List<String> collections = messageThreadPool.getCollection();
                    if (collections != null && !collections.isEmpty()) {
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
            if (defaultCollectionExecutorModel.defaultExecutor == null) {
                defaultCollectionExecutorModel.defaultExecutor = Executors.newVirtualThreadPerTaskExecutor();
            }
        }

        ExecutorService retrieveExecutor(Message message) {
            ExecutorService executor = null;
            if (message.getCollectionID() != null) {
                CollectionExecutorModel collectionExecutorModel =
                        collectionExecutorModelMap.get(message.getCollectionID());
                if (collectionExecutorModel != null) {
                    executor = collectionExecutorModel.retrieveExecutor(message);
                }
            }
            if (executor == null) {
                executor = defaultCollectionExecutorModel.retrieveExecutor(message);
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
                return Executors.newVirtualThreadPerTaskExecutor();
            } else if (poolSize.intValue() == 1) {
                return Executors.newSingleThreadExecutor(threadFactory);
            } else {
                return new ConcurrencyLimitedExecutorService(poolSize.intValue(), threadFactory);
            }
        }

        /**
         * Contain the executors for a single collection.
         */
        private class CollectionExecutorModel {
            private ExecutorService defaultExecutor;
            private final Map<MessageCategory, ExecutorService> categoryExecutorMap = new HashMap<>();
            private final Map<String, ExecutorService> messageExecutorMap = new HashMap<>();

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
                    defaultExecutor = executor;
                }
            }

            ExecutorService retrieveExecutor(Message message) {
                ExecutorService executor = messageExecutorMap.get(message.getClass().getSimpleName());
                if (executor == null) {
                    MessageCategory messageCategory = MessageCategoryUtils.getCategory(message);
                    executor = categoryExecutorMap.get(messageCategory);
                }
                if (executor == null) {
                    executor = defaultExecutor;
                }
                return executor;
            }

            void shutdown() {
                if (defaultExecutor != null) {
                    defaultExecutor.shutdown();
                }

                for (ExecutorService executor : messageExecutorMap.values()) {
                    executor.shutdown();
                }
            }
        }
    }
}
