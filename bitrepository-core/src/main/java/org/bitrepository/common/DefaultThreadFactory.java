package org.bitrepository.common;

import java.util.concurrent.ThreadFactory;

import org.slf4j.LoggerFactory;

/**
 * Creates thread for executors enforcing the general bit repository guidelines. These include<ul>
 *     <li>Setting thread priority factory configured value.</li>
 *     <li>Setting the thread name to the indicated prefix + a counter indentifying the number of threads created by
 *     this factory.</li>
 *     <li>Adds a error handler logging to the bit repository logger.</li>
 *     <li>Marks the thread as daemon thread.</li>
 *     </ul>
 */
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
        counter++;
        return newThread;
    }
}
