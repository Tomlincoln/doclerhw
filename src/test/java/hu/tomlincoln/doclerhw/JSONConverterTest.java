package hu.tomlincoln.doclerhw;

import java.util.HashMap;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JSONConverterTest {

    private static final String HOST = "8.8.8.8";
    private static final String EMPTY_RESULT = "{\"host\":\"8.8.8.8\", \"icmp ping\":\"NO_RESULT\", \"tcp ping\":\"NO_RESULT\", \"trace\":\"NO_RESULT\"}";
    private static final String UNESCAPE_RESULT = "{\"host\":\"8.8.8.8\", \"icmp ping\":\"_\\/\\\\\\\"\\b\\t\\n\\f\\r\", " +
            "\"tcp ping\":\"_\\/\\\\\\\"\\b\\t\\n\\f\\r\", \"trace\":\"_\\/\\\\\\\"\\b\\t\\n\\f\\r\"}";

    @Test
    public void toJson_ShouldWork() {
        HashMap<MonitoringType, MonitoringResult> mockedState = new HashMap<>();
        for (MonitoringType type : MonitoringType.values()) {
            mockedState.put(type, new MonitoringResult(App.NO_RESULT, App.NO_RESULT_DATE));
        }

        String actual = JSONConverter.toJSON(HOST, mockedState);

        Assertions.assertEquals(EMPTY_RESULT, actual);
    }

    @Test
    public void toJson_ShouldEscapeSpecialCharacters() {
        HashMap<MonitoringType, MonitoringResult> mockedState = new HashMap<>();
        String fullOfUnescaped = "_" + '/' + '\\' + '"' + '\b' + '\t' + '\n' + '\f' + '\r';
        for (MonitoringType type : MonitoringType.values()) {
            mockedState.put(type, new MonitoringResult(fullOfUnescaped, App.NO_RESULT_DATE));
        }

        String actual = JSONConverter.toJSON(HOST, mockedState);

        Assertions.assertEquals(UNESCAPE_RESULT, actual);
    }

}