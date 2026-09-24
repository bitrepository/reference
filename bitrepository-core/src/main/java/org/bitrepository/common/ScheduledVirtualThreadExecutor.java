/*
 * #%L
 * Bitrepository Protocol
 *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The Royal Library and The State Archives, Denmark
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

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
     * Dispatches {@code command} to a new virtual thread, waiting {@code delay} after each dispatched run
     * <em>finishes</em> before scheduling the next one, starting after {@code initialDelay}. Matches
     * {@link java.util.Timer#schedule}'s fixed-delay semantics: consecutive runs never overlap.
     * <p>
     * This can't be implemented as {@code ticker.scheduleWithFixedDelay(() -> worker.execute(command), ...)}: that
     * lambda returns as soon as it has handed {@code command} to the worker, not when {@code command} actually
     * finishes, so the ticker would re-arm on a fixed cadence regardless of how long {@code command} runs - i.e.
     * fixed-rate behaviour rather than fixed-delay. Instead, each run reschedules the next one itself once it
     * completes, from within its own virtual thread, so the platform-thread ticker is only ever asked to fire a
     * cheap one-shot callback and never blocks on {@code command}'s body.
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        FixedDelayFuture handle = new FixedDelayFuture();
        Runnable[] runOnWorker = new Runnable[1];
        runOnWorker[0] = () -> {
            try {
                command.run();
            } finally {
                rearm(handle, runOnWorker[0], delay, unit);
            }
        };
        handle.trySetCurrent(ticker.schedule(() -> worker.execute(runOnWorker[0]), initialDelay, unit));
        return handle;
    }

    private void rearm(FixedDelayFuture handle, Runnable runOnWorker, long delay, TimeUnit unit) {
        ScheduledFuture<?> next;
        try {
            next = ticker.schedule(() -> worker.execute(runOnWorker), delay, unit);
        } catch (RejectedExecutionException e) {
            // The ticker was shut down (close()) between this run finishing and rescheduling the next one.
            // Nothing to do - there's no next run to schedule.
            return;
        }
        if (!handle.trySetCurrent(next)) {
            // cancel() ran (or is running) concurrently with this rearm and won't see `next`, since it committed
            // to cancelling whatever `current` held before we could publish it. Cancel `next` ourselves instead.
            next.cancel(false);
        }
    }

    /**
     * A handle for a {@link #scheduleWithFixedDelay} chain: at any moment {@link #current} is the {@link
     * ScheduledFuture} for the next (or currently firing) one-shot dispatch. Cancelling this handle stops the
     * chain from rearming itself once the in-flight run, if any, finishes.
     * <p>
     * {@link #cancel} and {@link #trySetCurrent} run on different threads - the test/caller thread and the
     * dispatched task's virtual thread, respectively - and must be mutually exclusive: a lock, rather than
     * independent volatile fields, closes the window where one thread's "already scheduled" check would
     * otherwise race the other's cancellation, letting a run sneak in after cancel() returned.
     */
    private static final class FixedDelayFuture implements ScheduledFuture<Object> {
        private final Lock lock = new ReentrantLock();
        private ScheduledFuture<?> current;
        private boolean cancelled = false;

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean wasAlreadyCancelled;
            ScheduledFuture<?> future;
            lock.lock();
            try {
                wasAlreadyCancelled = cancelled;
                cancelled = true;
                future = current;
            } finally {
                lock.unlock();
            }
            if (future != null) {
                // Best-effort: cancels the pending one-shot dispatch, if any is currently pending rather than
                // running. A run already in progress is left to finish; trySetCurrent() will see cancelled ==
                // true and refuse to publish (and rearm() will then cancel) the next one.
                future.cancel(mayInterruptIfRunning);
            }
            return !wasAlreadyCancelled;
        }

        /**
         * Publishes {@code next} as the chain's current dispatch, unless {@link #cancel} has already run - in
         * which case it refuses, so the caller can cancel {@code next} itself instead.
         */
        private boolean trySetCurrent(ScheduledFuture<?> next) {
            lock.lock();
            try {
                if (cancelled) {
                    return false;
                }
                current = next;
                return true;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean isCancelled() {
            lock.lock();
            try {
                return cancelled;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public boolean isDone() {
            return isCancelled();
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
            throw new UnsupportedOperationException("A recurring fixed-delay task never completes; cancel it instead of waiting on it.");
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw new UnsupportedOperationException("A recurring fixed-delay task never completes; cancel it instead of waiting on it.");
        }

        @Override
        public long getDelay(TimeUnit unit) {
            ScheduledFuture<?> future;
            lock.lock();
            try {
                future = current;
            } finally {
                lock.unlock();
            }
            return future != null ? future.getDelay(unit) : 0L;
        }

        @Override
        public int compareTo(Delayed o) {
            return Long.compare(getDelay(TimeUnit.NANOSECONDS), o.getDelay(TimeUnit.NANOSECONDS));
        }
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
