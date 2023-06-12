package hu.tomlincoln.doclerhw;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class App {

    public static final String NO_RESULT = "NO_RESULT";
    public static final LocalDateTime NO_RESULT_DATE = LocalDateTime.MIN;
    private static final Map<String, Map<MonitoringType, MonitoringResult>> STATE = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        PingApplication application = new PingApplication(new Properties(), null);
        application.start();
    }

    public static Map<MonitoringType, MonitoringResult> getState(String hostname) {
        synchronized (App.class) {
            if (STATE.get(hostname) == null) {
                Map<MonitoringType, MonitoringResult> result = new HashMap<>();
                for (MonitoringType type : MonitoringType.values()) {
                    result.put(type, new MonitoringResult(NO_RESULT, NO_RESULT_DATE));
                }
                STATE.put(hostname, result);
            }
        }
        return STATE.get(hostname);
    }

}
