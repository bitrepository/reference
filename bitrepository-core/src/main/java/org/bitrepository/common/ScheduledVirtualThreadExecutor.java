/*
 * #%L
 * Bitmagasin integrationstest
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
package org.bitrepository.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Schedules recurring or delayed tasks the way {@link java.util.Timer} used to, but dispatches each firing to its
 * own virtual thread instead of running it on the scheduling thread itself. A single-thread, named platform-thread
 * {@link ScheduledExecutorService} acts purely as the "ticker" - it never runs task bodies itself, so a slow or
 * blocking task can't delay the next scheduled firing, and the ticker thread is never split up amongst virtual
 * threads.
 * <p>
 * Uncaught exceptions escaping a dispatched task are logged and otherwise ignored, so one failing run doesn't stop
 * future runs - unlike {@link java.util.Timer}, where an uncaught exception silently kills the whole timer thread.
 */
public class ScheduledVirtualThreadExecutor implements AutoCloseable {
    private final ScheduledExecutorService ticker;
    private final ExecutorService worker;

    /**
     * @param name   Prefix used for both the ticker thread's name and the dispatched virtual threads' names.
     * @param daemon Whether the ticker thread should be a daemon thread.
     */
    public ScheduledVirtualThreadExecutor(String name, boolean daemon) {
        ticker = Executors.newSingleThreadScheduledExecutor(new DefaultThreadFactory(name, Thread.NORM_PRIORITY, daemon));
        worker = Executors.newThreadPerTaskExecutor(Thread.ofVirtual().name(name + "-worker-", 0)
                .uncaughtExceptionHandler(ScheduledVirtualThreadExecutor::logUncaughtException)
                .factory());
    }

    private static void logUncaughtException(Thread thread, Throwable throwable) {
        String throwingClass = throwable.getStackTrace()[0].getClassName();
        Logger logger = LoggerFactory.getLogger(throwingClass);
        logger.error("UncaughtExceptionHandler caught Exception:", throwable);
    }

    /**
     * Dispatches {@code command} to a new virtual thread once, after {@code delay}.
     */
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return ticker.schedule(() -> worker.execute(command), delay, unit);
    }

    /**
     * Dispatches {@code command} to a new virtual thread every {@code period}, starting after {@code initialDelay}.
     * Ticks continue at the fixed rate regardless of how long a dispatched run takes, matching
     * {@link java.util.Timer#scheduleAtFixedRate}.
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return ticker.scheduleAtFixedRate(() -> worker.execute(command), initialDelay, period, unit);
    }

    /**
     * Dispatches {@code command} to a new virtual thread, waiting {@code delay} after each dispatch before
     * scheduling the next one, starting after {@code initialDelay}. Matches {@link java.util.Timer#schedule}'s
     * fixed-delay semantics.
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return ticker.scheduleWithFixedDelay(() -> worker.execute(command), initialDelay, delay, unit);
    }

    /**
     * Stops the ticker from scheduling further work and stops accepting new dispatches. Tasks already dispatched
     * to a virtual thread are left to finish on their own.
     */
    @Override
    public void close() {
        ticker.shutdownNow();
        worker.shutdown();
    }
}
