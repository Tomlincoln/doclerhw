package hu.tomlincoln.doclerhw;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import hu.tomlincoln.doclerhw.JSONConverter;
import hu.tomlincoln.doclerhw.MonitoringResult;
import hu.tomlincoln.doclerhw.MonitoringType;
import hu.tomlincoln.doclerhw.Reporter;

import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.openMocks;

class ReporterTest {

    private static final String REPORT_URL = "http://127.0.0.1:8080/report";
    private static final String HOST = "8.8.8.8";
    private static final Integer TCP_TIMEOUT = 60000;

    @Mock
    private URL url;

    @Mock
    private HttpURLConnection connection;

    @Mock
    private OutputStream outputStream;

    @BeforeEach
    public void before() {
        openMocks(this);
    }

    @Test
    public void whenCallReport_ItWorks() throws IOException {

        // GIVEN

        Mockito.when(url.openConnection()).thenReturn(connection);
        Mockito.when(url.toString()).thenReturn("http://127.0.0.1:8080/report");
        Mockito.when(connection.getResponseCode()).thenReturn(200);
        Mockito.when(connection.getOutputStream()).thenReturn(outputStream);
        HashMap<MonitoringType, MonitoringResult> mockedState = new HashMap<>();
        mockedState.put(MonitoringType.ICMP, new MonitoringResult(App.NO_RESULT, App.NO_RESULT_DATE));
        mockedState.put(MonitoringType.TCP, new MonitoringResult(App.NO_RESULT, App.NO_RESULT_DATE));
        mockedState.put(MonitoringType.TRACE, new MonitoringResult("asd", App.NO_RESULT_DATE));

        try (MockedStatic<JSONConverter> jsonMockedStatic = Mockito.mockStatic(JSONConverter.class);
             MockedStatic<App> appMockedStatic = Mockito.mockStatic(App.class)) {
            jsonMockedStatic.when(() -> JSONConverter.toJSON(HOST, mockedState)).thenReturn("asd");
            appMockedStatic.when(() -> App.getState(HOST)).thenReturn(mockedState);

        // WHEN

            Reporter reporter = new Reporter(REPORT_URL, url);
            reporter.report(HOST);

        // THEN

            appMockedStatic.verify(() -> App.getState(HOST));
            jsonMockedStatic.verify(() -> JSONConverter.toJSON(HOST, mockedState));
        }

        Mockito.verify(connection, times(1)).setConnectTimeout(TCP_TIMEOUT);
        Mockito.verify(connection, times(1)).setReadTimeout(TCP_TIMEOUT);
        Mockito.verify(connection, times(1)).setRequestMethod("POST");
        Mockito.verify(connection, times(1)).setRequestProperty("Content-Type", "application/json");
        Mockito.verify(connection, times(1)).setDoOutput(true);
        Mockito.verify(connection, times(1)).getOutputStream();
        Mockito.verify(outputStream, times(1)).write("asd".getBytes());
        Mockito.verify(outputStream, times(1)).flush();
        Mockito.verify(connection, times(1)).getResponseCode();

    }

}