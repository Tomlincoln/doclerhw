package hu.tomlincoln.doclerhw;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Logger;

public class Reporter {

    private static final Logger log = Logger.getLogger("");
    private final String reportUrl;
    private URL url = null;

    public Reporter(String reportUrl) {
        this.reportUrl = reportUrl;
    }

    Reporter (String reportUrl, URL url) {
        this(reportUrl);
        this.url = url;
    }

    public void report(String hostname) {
        Map<MonitoringType, MonitoringResult> hostNameResult = App.getState(hostname);
        synchronized (App.class) {
            doReportHttp(hostname, hostNameResult);
            doReportLocal(hostname, hostNameResult);
        }
    }

    private void doReportHttp(String hostname, Map<MonitoringType, MonitoringResult> hostNameResult) {
        HttpURLConnection connection = null;
        int responseCode = -1;
        OutputStream os = null;
        try {
            String jsonToPost = JSONConverter.toJSON(hostname, hostNameResult);
            if (url == null) {
                url = new URL(reportUrl);
            }
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            os = connection.getOutputStream();
            os.write(jsonToPost.getBytes());
            os.flush();
            responseCode = connection.getResponseCode();
        } catch (IOException e) {
            log.fine("Exception happened during reporting over http: " + e.getMessage());
        } finally {
            try {
                if (os != null)
                    os.close();
            } catch (IOException e) {
                log.fine("Exception happened during closing output stream when reporting over http: " + e.getMessage());
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        if (!(responseCode == HttpURLConnection.HTTP_OK)) {
            log.fine("Reporting over HTTP failed with status code: " + responseCode);
        }
    }

    private void doReportLocal(String hostname, Map<MonitoringType, MonitoringResult> hostNameResult) {
        for (MonitoringType type : MonitoringType.values()) {
            if (hostNameResult.get(type).getLastResult().equals(App.NO_RESULT)) {
                log.warning("Currently have no result for type " + type.name() + " on host: " + hostname);
            } else {
                LocalDateTime lastResultDateTime = hostNameResult.get(type).getLastResultUpdated();
                String lastResultDateTimeFormatted = lastResultDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                log.warning("Last " + type.name() + "-type monitoring event happened on: " + lastResultDateTimeFormatted);
                log.warning(hostNameResult.get(type).getLastResult());
            }
        }
    }
}
