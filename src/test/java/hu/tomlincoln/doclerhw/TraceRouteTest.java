package hu.tomlincoln.doclerhw;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.openMocks;

class TraceRouteTest {

    private static final String TRACE_COMMAND = "tracert";
    private static final String HOST = "8.8.8.8";
    private static final String COMPLETE_TRACE_COMMAND = TRACE_COMMAND + " " + HOST;
    private static final String DNS_FAILURE_LINE = TraceRoute.DNS_FAILURE + HOST + ".\n";

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
    public void whenStartedNormally_thenExecutesTraceCommandOnRuntime() throws IOException {
        InputStream testData = new ByteArrayInputStream("test data".getBytes());
        try (MockedStatic<Runtime> runtimeMockedStatic = Mockito.mockStatic(Runtime.class)) {
            runtimeMockedStatic.when(Runtime::getRuntime).thenReturn(runtime);
            Mockito.when(runtime.exec(COMPLETE_TRACE_COMMAND)).thenReturn(process);
            Mockito.when(process.getInputStream()).thenReturn(testData);

            TraceRoute traceRoute = new TraceRoute(TRACE_COMMAND, HOST, reporter);
            traceRoute.run();

            runtimeMockedStatic.verify(Runtime::getRuntime, times(1));
            Mockito.verify(runtime, times(1)).exec(COMPLETE_TRACE_COMMAND);
        }
    }

    @Test
    public void whenStartedNormally_thenStateAfterRunShouldContainLastResult() throws IOException {
        InputStream testData = new ByteArrayInputStream("test data\n".getBytes());
        HashMap<MonitoringType, MonitoringResult> mockedState = new HashMap<>();
        for (MonitoringType type : MonitoringType.values()) {
            mockedState.put(type, new MonitoringResult(App.NO_RESULT, App.NO_RESULT_DATE));
        }
        try (MockedStatic<Runtime> runtimeMockedStatic = Mockito.mockStatic(Runtime.class);
             MockedStatic<App> appMockedStatic = Mockito.mockStatic(App.class)) {
            appMockedStatic.when(() -> App.getState(HOST)).thenReturn(mockedState);
            runtimeMockedStatic.when(Runtime::getRuntime).thenReturn(runtime);
            Mockito.when(runtime.exec(COMPLETE_TRACE_COMMAND)).thenReturn(process);
            Mockito.when(process.getInputStream()).thenReturn(testData);

            TraceRoute traceRoute = new TraceRoute(TRACE_COMMAND, HOST, reporter);
            traceRoute.run();

            Assertions.assertEquals("test data\n", mockedState.get(MonitoringType.TRACE).getLastResult());
        }
    }

    @Test
    public void whenFailedNameResolution_thenShouldReportAndStateAfterRunShouldContainLastResult() throws IOException {
        InputStream testData = new ByteArrayInputStream(DNS_FAILURE_LINE.getBytes());
        HashMap<MonitoringType, MonitoringResult> mockedState = new HashMap<>();
        for (MonitoringType type : MonitoringType.values()) {
            mockedState.put(type, new MonitoringResult(App.NO_RESULT, App.NO_RESULT_DATE));
        }
        try (MockedStatic<Runtime> runtimeMockedStatic = Mockito.mockStatic(Runtime.class);
             MockedStatic<App> appMockedStatic = Mockito.mockStatic(App.class)) {
            runtimeMockedStatic.when(Runtime::getRuntime).thenReturn(runtime);
            appMockedStatic.when(() -> App.getState(HOST)).thenReturn(mockedState);
            Mockito.when(runtime.exec(COMPLETE_TRACE_COMMAND)).thenReturn(process);
            Mockito.when(process.getInputStream()).thenReturn(testData);

            TraceRoute traceRoute = new TraceRoute(TRACE_COMMAND, HOST, reporter);
            traceRoute.run();

            Mockito.verify(reporter).report(HOST);
            Assertions.assertEquals(DNS_FAILURE_LINE, mockedState.get(MonitoringType.TRACE).getLastResult());
        }
    }

    @Test
    public void whenLastLineIsNotTraceComplete_thenShouldReportAndStateAfterRunShouldContainLastResult() throws IOException {
        String state = "1\n2\n3\n";
        InputStream testData = new ByteArrayInputStream(state.getBytes());
        HashMap<MonitoringType, MonitoringResult> mockedState = new HashMap<>();
        for (MonitoringType type : MonitoringType.values()) {
            mockedState.put(type, new MonitoringResult(App.NO_RESULT, App.NO_RESULT_DATE));
        }
        try (MockedStatic<Runtime> runtimeMockedStatic = Mockito.mockStatic(Runtime.class);
             MockedStatic<App> appMockedStatic = Mockito.mockStatic(App.class)) {
            runtimeMockedStatic.when(Runtime::getRuntime).thenReturn(runtime);
            appMockedStatic.when(() -> App.getState(HOST)).thenReturn(mockedState);
            Mockito.when(runtime.exec(COMPLETE_TRACE_COMMAND)).thenReturn(process);
            Mockito.when(process.getInputStream()).thenReturn(testData);

            TraceRoute traceRoute = new TraceRoute(TRACE_COMMAND, HOST, reporter);
            traceRoute.run();

            Mockito.verify(reporter).report(HOST);
            Assertions.assertEquals(state, mockedState.get(MonitoringType.TRACE).getLastResult());
        }
    }

}