package hu.tomlincoln.doclerhw;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import hu.tomlincoln.doclerhw.IcmpPing;
import hu.tomlincoln.doclerhw.MonitoringResult;
import hu.tomlincoln.doclerhw.MonitoringType;
import hu.tomlincoln.doclerhw.Reporter;

import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.openMocks;

class IcmpPingTest {

    private static final String PING_COMMAND = "ping";
    private static final String HOST = "8.8.8.8";
    private static final String COMPLETE_PING_COMMAND = PING_COMMAND + " " + IcmpPing.EXTRA_ARGUMENTS + " " + HOST;
    private static final String DNS_FAILURE_LINE = IcmpPing.DNS_FAILURE + HOST + "\n";

    @Mock
    private Runtime runtime;

    @Mock
    private Process process;

    @Mock
    private Reporter reporter;

    @BeforeEach
    public void before() {
        openMocks(this);
    }

    @Test
    public void whenStartedNormally_thenExecutesPingCommandOnRuntime() throws IOException {
        InputStream testData = new ByteArrayInputStream("test data".getBytes());
        try (MockedStatic<Runtime> runtimeMockedStatic = Mockito.mockStatic(Runtime.class)) {
            runtimeMockedStatic.when(Runtime::getRuntime).thenReturn(runtime);
            Mockito.when(runtime.exec(COMPLETE_PING_COMMAND)).thenReturn(process);
            Mockito.when(process.getInputStream()).thenReturn(testData);

            IcmpPing icmpPing = new IcmpPing(PING_COMMAND, HOST, reporter);
            icmpPing.run();

            runtimeMockedStatic.verify(Runtime::getRuntime, times(1));
            Mockito.verify(runtime, times(1)).exec(COMPLETE_PING_COMMAND);
        }
    }

    @Test
    public void whenStartedNormally_thenStateAfterRunShouldContainLastResult() throws IOException {
        InputStream testData = new ByteArrayInputStream("test data\n".getBytes());
        HashMap <MonitoringType, MonitoringResult> mockedState = new HashMap<>();
        for (MonitoringType type : MonitoringType.values()) {
            mockedState.put(type, new MonitoringResult(App.NO_RESULT, App.NO_RESULT_DATE));
        }
        try (MockedStatic<Runtime> runtimeMockedStatic = Mockito.mockStatic(Runtime.class);
             MockedStatic<App> appMockedStatic = Mockito.mockStatic(App.class)) {
            appMockedStatic.when(()->App.getState(HOST)).thenReturn(mockedState);
            runtimeMockedStatic.when(Runtime::getRuntime).thenReturn(runtime);
            Mockito.when(runtime.exec(COMPLETE_PING_COMMAND)).thenReturn(process);
            Mockito.when(process.getInputStream()).thenReturn(testData);

            IcmpPing icmpPing = new IcmpPing(PING_COMMAND, HOST, reporter);
            icmpPing.run();

            Assertions.assertEquals("test data\n", mockedState.get(MonitoringType.ICMP).getLastResult());
        }
    }

    @Test
    public void whenFailedNameResolution_thenShouldReportAndStateAfterRunShouldContainLastResult() throws IOException {
        InputStream testData = new ByteArrayInputStream(DNS_FAILURE_LINE.getBytes());
        HashMap <MonitoringType, MonitoringResult> mockedState = new HashMap<>();
        for (MonitoringType type : MonitoringType.values()) {
            mockedState.put(type, new MonitoringResult(App.NO_RESULT, App.NO_RESULT_DATE));
        }
        try (MockedStatic<Runtime> runtimeMockedStatic = Mockito.mockStatic(Runtime.class);
             MockedStatic<App> appMockedStatic = Mockito.mockStatic(App.class)) {
            runtimeMockedStatic.when(Runtime::getRuntime).thenReturn(runtime);
            appMockedStatic.when(()->App.getState(HOST)).thenReturn(mockedState);
            Mockito.when(runtime.exec(COMPLETE_PING_COMMAND)).thenReturn(process);
            Mockito.when(process.getInputStream()).thenReturn(testData);

            IcmpPing icmpPing = new IcmpPing(PING_COMMAND, HOST, reporter);
            icmpPing.run();

            Mockito.verify(reporter).report(HOST);
            Assertions.assertEquals(DNS_FAILURE_LINE, mockedState.get(MonitoringType.ICMP).getLastResult());
        }
    }

    @Test
    public void whenPacketLoss_thenShouldReportAndStateAfterRunShouldContainLastResult() throws IOException {
        StringBuilder sb = new StringBuilder();
        IntStream.range(1,10).forEach(i -> sb.append(i).append("\n"));
        sb.append(IcmpPing.ALL_OK_AFTER_5_PING.replace("0% loss","20% loss")).append("\n");
        String state = sb.toString();
        InputStream testData = new ByteArrayInputStream(state.getBytes());
        HashMap <MonitoringType, MonitoringResult> mockedState = new HashMap<>();
        for (MonitoringType type : MonitoringType.values()) {
            mockedState.put(type, new MonitoringResult(App.NO_RESULT, App.NO_RESULT_DATE));
        }
        try (MockedStatic<Runtime> runtimeMockedStatic = Mockito.mockStatic(Runtime.class);
             MockedStatic<App> appMockedStatic = Mockito.mockStatic(App.class)) {
            runtimeMockedStatic.when(Runtime::getRuntime).thenReturn(runtime);
            appMockedStatic.when(()->App.getState(HOST)).thenReturn(mockedState);
            Mockito.when(runtime.exec(COMPLETE_PING_COMMAND)).thenReturn(process);
            Mockito.when(process.getInputStream()).thenReturn(testData);

            IcmpPing icmpPing = new IcmpPing(PING_COMMAND, HOST, reporter);
            icmpPing.run();

            Mockito.verify(reporter).report(HOST);
            Assertions.assertEquals(state, mockedState.get(MonitoringType.ICMP).getLastResult());
        }
    }

}