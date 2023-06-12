package hu.tomlincoln.doclerhw;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import hu.tomlincoln.doclerhw.MonitoringResult;
import hu.tomlincoln.doclerhw.MonitoringType;
import hu.tomlincoln.doclerhw.Reporter;
import hu.tomlincoln.doclerhw.TcpPing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.openMocks;

class TcpPingTest {

    private static final String HOST = "8.8.8.8";
    private static final Integer TCP_TIMEOUT = 1000;

    @Mock
    private URL url;

    @Mock
    private Reporter reporter;

    @Mock
    private HttpURLConnection connection;

    @BeforeEach
    public void before() {
        openMocks(this);
    }

    @Test
    public void whenStartedNormally_thenExecutesTcpPing() throws IOException {
        HashMap<MonitoringType, MonitoringResult> mockedState = new HashMap<>();
        for (MonitoringType type : MonitoringType.values()) {
            mockedState.put(type, new MonitoringResult(App.NO_RESULT, App.NO_RESULT_DATE));
        }
        try (MockedStatic<App> appMockedStatic = Mockito.mockStatic(App.class)) {

            Mockito.when(url.openConnection()).thenReturn(connection);
            Mockito.when(url.toString()).thenReturn("http://8.8.8.8");
            Mockito.when(connection.getResponseCode()).thenReturn(200);
            appMockedStatic.when(() -> App.getState(HOST)).thenReturn(mockedState);

            TcpPing tcpPing = new TcpPing(HOST, TCP_TIMEOUT, reporter, url);
            tcpPing.run();

            appMockedStatic.verify(() -> App.getState(HOST), times(1));
        }
        Mockito.verify(connection, times(1)).setConnectTimeout(TCP_TIMEOUT);
        Mockito.verify(connection, times(1)).setReadTimeout(TCP_TIMEOUT);
        Mockito.verify(connection, times(1)).connect();
        Mockito.verify(connection, times(1)).getResponseCode();
        String lastResult = mockedState.get(MonitoringType.TCP).getLastResult();
        Assertions.assertTrue(lastResult.startsWith("URL: http://8.8.8.8, round time: "));
        Assertions.assertTrue(lastResult.endsWith(" ms, status code was 200"));
    }

    @Test
    public void whenConnectionFails_thenStateShouldContainExceptionMessageAndShouldReport() throws IOException {
        HashMap<MonitoringType, MonitoringResult> mockedState = new HashMap<>();
        for (MonitoringType type : MonitoringType.values()) {
            mockedState.put(type, new MonitoringResult(App.NO_RESULT, App.NO_RESULT_DATE));
        }
        try (MockedStatic<App> appMockedStatic = Mockito.mockStatic(App.class)) {
            Mockito.when(url.openConnection()).thenReturn(connection);
            Mockito.when(url.toString()).thenReturn("http://8.8.8.8");
            Mockito.doThrow(new IOException("asd")).when(connection).connect();
            appMockedStatic.when(() -> App.getState(HOST)).thenReturn(mockedState);

            TcpPing tcpPing = new TcpPing(HOST, TCP_TIMEOUT, reporter, url);
            tcpPing.run();

            appMockedStatic.verify(() -> App.getState(HOST), times(1));
        }
        Mockito.verify(connection, times(1)).setConnectTimeout(TCP_TIMEOUT);
        Mockito.verify(connection, times(1)).setReadTimeout(TCP_TIMEOUT);
        Mockito.verify(connection, times(1)).connect();
        Mockito.verify(reporter, times(1)).report(HOST);
        assertEquals("Exception: asd", mockedState.get(MonitoringType.TCP).getLastResult());
    }

}