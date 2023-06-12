package hu.tomlincoln.doclerhw;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import hu.tomlincoln.doclerhw.PingApplication;
import hu.tomlincoln.doclerhw.Reporter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.openMocks;

class PingApplicationTest {

    private static final String APP_HOSTS_NAME = "app.hosts";
    private static final String ICMP_DELAY_NAME = "icmp.delay";
    private static final String ICMP_COMMAND_NAME = "icmp.command";
    private static final String TCP_DELAY_NAME = "tcp.delay";
    private static final String TCP_TIMEOUT_NAME = "tcp.timeout";
    private static final String TRACE_DELAY_NAME = "trace.delay";
    private static final String TRACE_COMMAND_NAME = "trace.command";
    private static final String REPORT_URL_NAME = "report.url";
    private static final String LOG_LOCATION_NAME = "log.location";

    @Mock
    private ScheduledThreadPoolExecutor executor;

    @Mock
    private Properties properties;

    @BeforeEach
    public void before() {
        openMocks(this);
    }

    private Properties createDefaultProperties() {
        Properties properties = new Properties();
        properties.setProperty(APP_HOSTS_NAME, "aaaaasd.hu");
        properties.setProperty(ICMP_DELAY_NAME, "1");
        properties.setProperty(ICMP_COMMAND_NAME, "ping");
        properties.setProperty(TCP_DELAY_NAME, "1");
        properties.setProperty(TCP_TIMEOUT_NAME, "1000");
        properties.setProperty(TRACE_DELAY_NAME, "1");
        properties.setProperty(TRACE_COMMAND_NAME, "traceroute");
        properties.setProperty(REPORT_URL_NAME, ".");
        properties.setProperty(LOG_LOCATION_NAME, "main.log");
        return properties;
    }

    @Test
    public void whenStartedNormally_thenCreatesThreeFixedRateJobForOneHost() {
        try (MockedStatic<Executors> utilities = Mockito.mockStatic(Executors.class)) {
            utilities.when(() -> Executors.newScheduledThreadPool(anyInt())).thenReturn(executor);
            Properties customProperties = createDefaultProperties();
            customProperties.setProperty(APP_HOSTS_NAME, "8.8.8.8");

            PingApplication myApp = new PingApplication(customProperties, new Reporter("."));
            myApp.start();

            utilities.verify(() -> Executors.newScheduledThreadPool(anyInt()), times(3));
            Mockito.verify(executor, times(3)).scheduleAtFixedRate(any(), anyLong(), anyLong(), any());
        }
    }

    @Test
    public void whenStartedNormally_thenCreatesSixFixedRateJobForTwoHost() {
        try (MockedStatic<Executors> utilities = Mockito.mockStatic(Executors.class)) {
            utilities.when(() -> Executors.newScheduledThreadPool(anyInt())).thenReturn(executor);
            Properties customProperties = createDefaultProperties();
            customProperties.setProperty(APP_HOSTS_NAME, "8.8.8.8,4.4.4.4");

            PingApplication myApp = new PingApplication(customProperties, new Reporter("."));
            myApp.start();

            utilities.verify(() -> Executors.newScheduledThreadPool(anyInt()), times(6));
            Mockito.verify(executor, times(6)).scheduleAtFixedRate(any(), anyLong(), anyLong(), any());
        }
    }

    @Test
    public void whenIOExceptionLoadingProperties_thenThrowsErrorAndThreadsNotStarting() {
        try (MockedStatic<Executors> utilities = Mockito.mockStatic(Executors.class)) {
            utilities.when(() -> Executors.newScheduledThreadPool(anyInt())).thenReturn(executor);
            Mockito.when(properties.getProperty("log.location")).thenAnswer(invocation -> {
                throw new IOException();
            });

            Assertions.assertThrows(Error.class, () -> {
                PingApplication myApp = new PingApplication(properties, new Reporter("."));
                myApp.start();
            }, "Error was expected");

            utilities.verify(() -> Executors.newScheduledThreadPool(anyInt()), times(0));
            Mockito.verify(executor, times(0)).scheduleAtFixedRate(any(), anyLong(), anyLong(), any());
        }
    }

    @Test
    public void whenIOExceptionSettingLogFile_thenThrowsErrorAndThreadsNotStarting() {
        try (MockedStatic<Executors> utilities = Mockito.mockStatic(Executors.class)) {
            utilities.when(() -> Executors.newScheduledThreadPool(anyInt())).thenReturn(executor);
            doAnswer(invocation -> {
                throw new IOException();
            }).when(properties).load(any(InputStream.class));
            doReturn(true).when(properties).isEmpty();

            Assertions.assertThrows(Error.class, () -> {
                PingApplication myApp = new PingApplication(properties, new Reporter("."));
                myApp.start();
            }, "Error was expected");

            utilities.verify(() -> Executors.newScheduledThreadPool(anyInt()), times(0));
            Mockito.verify(executor, times(0)).scheduleAtFixedRate(any(), anyLong(), anyLong(), any());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}