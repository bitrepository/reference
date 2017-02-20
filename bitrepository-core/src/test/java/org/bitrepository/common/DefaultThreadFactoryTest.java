package org.bitrepository.common;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import org.mockito.ArgumentCaptor;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
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
        ch.qos.logback.classic.Logger rootLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(
                ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);

        //We mock an appender so we can catch the log messages
        final Appender<ILoggingEvent> mockAppender = mock(Appender.class);

        //Nessesary for the logback framework, all appenders must have a name
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

        assertThat( logLine.getLevel(), is( equalTo( Level.ERROR)));

        assertThat( logLine.getLoggerName(), is( equalTo( DefaultThreadFactoryTest.class.getName())));

        assertThat( logLine.getThrowableProxy().getMessage(), is( equalTo( DefaultThreadFactoryTest.this.message)));
    }
}