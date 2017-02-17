package org.bitrepository.common;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import org.mockito.ArgumentMatcher;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by abr on 17-02-17.
 */
public class DefaultThreadFactoryTest {

    private final String message = "Hey this is the message I want to see";

    @Test(groups = {"regressiontest"})
    public void testUncaughtExceptionHandler() throws Exception {
        // Technique from https://dzone.com/articles/unit-testing-asserting-line

        //Get the logger as a logback logger so we can set properties on it
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
                ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

        //We mock an appender so we can catch the log messages
        final Appender<ILoggingEvent> mockAppender = mock(Appender.class);
        when(mockAppender.getName()).thenReturn("MOCK");
        root.addAppender(mockAppender);

        //Create the new thread factory
        DefaultThreadFactory factory = new DefaultThreadFactory(this.getClass().getSimpleName(), Thread.NORM_PRIORITY,
                                                                false);

        //Create a new thread that throws a runtime exception with a specific message
        Thread thread = factory.newThread(() -> {
            throw new RuntimeException(message);
        });
        //Start the thread and wait for it to die.
        thread.start();
        thread.join();

        //An error message should now have been logged
        verify(mockAppender).doAppend(argThat(new ArgumentMatcher<LoggingEvent>() {
            @Override
            public boolean matches(final Object argument) {
                LoggingEvent loggingEvent = (LoggingEvent) argument;

                boolean level = loggingEvent.getLevel() == Level.ERROR;

                String expectedName = DefaultThreadFactoryTest.class.getName();
                String loggerName = loggingEvent.getLoggerName();
                boolean name = loggerName.equals(expectedName);

                IThrowableProxy throwException = loggingEvent.getThrowableProxy();
                boolean message = throwException.getMessage().equals(DefaultThreadFactoryTest.this.message);
                return level && name && message;
            }
        }));
    }
}