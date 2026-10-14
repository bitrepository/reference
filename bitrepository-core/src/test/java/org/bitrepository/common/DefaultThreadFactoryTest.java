package org.bitrepository.common;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.bitrepository.TestGroups;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by abr on 17-02-17.
 */
class DefaultThreadFactoryTest {

    private final String message = "Hey this is the message I want to see";

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void testUncaughtExceptionHandler() throws Exception {
        // Technique from https://dzone.com/articles/unit-testing-asserting-line

        //Get the logger as a logback logger so we can set properties on it
        Logger rootLogger = (Logger) LoggerFactory.getLogger(
                Logger.ROOT_LOGGER_NAME);

        //We mock an appender so we can catch the log messages
        @SuppressWarnings("unchecked")
        final Appender<ILoggingEvent> mockAppender = mock(Appender.class);

        //Necessary for the logback framework, all appenders must have a name
        when(mockAppender.getName()).thenReturn("MOCK");
        //Add the appender to the root logger
        rootLogger.addAppender(mockAppender);

        //Setup a argumentCaptor for the ILoggingEvent that the appender will be called with
        ArgumentCaptor<ILoggingEvent> argument = ArgumentCaptor.forClass(ILoggingEvent.class);


        //Create the new thread factory
        DefaultThreadFactory factory = new DefaultThreadFactory(this.getClass().getSimpleName(), Thread.NORM_PRIORITY,
                                                                false);

        //Create a new thread that throws a runtime exception with a specific message
        Thread thread = factory.newThread(() -> {
            throw new RuntimeException(message);
        });
        //Start the thread
        thread.start();
        //Wait for the thread to die
        thread.join();

        //Capture the argument from the appender
        verify(mockAppender).doAppend(argument.capture());
        ILoggingEvent logLine = argument.getValue();

        assertEquals(Level.ERROR, logLine.getLevel());
        assertEquals(DefaultThreadFactoryTest.class.getName(), logLine.getLoggerName());
        assertEquals(message, logLine.getThrowableProxy().getMessage());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void testVirtualThreadBuilderLogsUncaughtExceptions() throws Exception {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(
                Logger.ROOT_LOGGER_NAME);
        @SuppressWarnings("unchecked")
        final Appender<ILoggingEvent> mockAppender = mock(Appender.class);
        rootLogger.addAppender(mockAppender);
        ArgumentCaptor<ILoggingEvent> argument = ArgumentCaptor.forClass(ILoggingEvent.class);

        Thread thread;
        try {
            thread = DefaultThreadFactory.virtualThreadBuilder().name("virtual-", 0).unstarted(() -> {
                throw new RuntimeException(message);
            });
            thread.start();
            thread.join();
            verify(mockAppender).doAppend(argument.capture());
        } finally {
            rootLogger.detachAppender(mockAppender);
        }

        assertThat(thread.isVirtual(), is(true));
        assertThat(thread.getName(), startsWith("virtual-"));
        ILoggingEvent logLine = argument.getValue();
        assertThat(logLine.getLevel(), is(equalTo(Level.ERROR)));
        assertThat(logLine.getLoggerName(), is(equalTo(DefaultThreadFactoryTest.class.getName())));
        assertThat(logLine.getThrowableProxy().getMessage(), is(equalTo(DefaultThreadFactoryTest.this.message)));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void testUncaughtExceptionWithoutStackTraceIsStillLogged() throws Exception {
        Logger rootLogger = (Logger) LoggerFactory.getLogger(
                Logger.ROOT_LOGGER_NAME);
        @SuppressWarnings("unchecked")
        final Appender<ILoggingEvent> mockAppender = mock(Appender.class);
        rootLogger.addAppender(mockAppender);
        ArgumentCaptor<ILoggingEvent> argument = ArgumentCaptor.forClass(ILoggingEvent.class);

        RuntimeException withoutStackTrace = new RuntimeException(message);
        withoutStackTrace.setStackTrace(new StackTraceElement[0]);
        try {
            Thread thread = DefaultThreadFactory.virtualThreadBuilder().unstarted(() -> {
                throw withoutStackTrace;
            });
            thread.start();
            thread.join();
            verify(mockAppender).doAppend(argument.capture());
        } finally {
            rootLogger.detachAppender(mockAppender);
        }

        ILoggingEvent logLine = argument.getValue();
        assertThat(logLine.getLevel(), is(equalTo(Level.ERROR)));
        assertThat(logLine.getLoggerName(), is(equalTo(DefaultThreadFactory.class.getName())));
        assertThat(logLine.getThrowableProxy().getMessage(), is(equalTo(DefaultThreadFactoryTest.this.message)));
    }
}