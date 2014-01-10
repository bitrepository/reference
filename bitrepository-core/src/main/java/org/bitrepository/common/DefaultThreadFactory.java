package org.bitrepository.common;

import java.util.concurrent.ThreadFactory;

import org.slf4j.LoggerFactory;

public class DefaultThreadFactory implements ThreadFactory {
    private final String prefix;
    private final int priority;
    private int counter = 0;

    public DefaultThreadFactory(String prefix, int priority) {
        this.prefix = prefix;
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread newThread = new Thread(r, prefix + "-Thread" +counter);
        newThread.setPriority(priority);
        newThread.setDaemon(true);
        newThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LoggerFactory.getLogger(t.getName()).error(e.getMessage(), e);
            }
        });
        return newThread;
    }
}
