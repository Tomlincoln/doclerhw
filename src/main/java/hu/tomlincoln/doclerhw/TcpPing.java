package hu.tomlincoln.doclerhw;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class TcpPing extends MonitoringItem {

    private static final Logger log = Logger.getLogger("");
    private final Integer tcpTimeout;
    private URL url;

    public TcpPing(String hostname, Integer tcpTimeout, Reporter reporter) {
        super(hostname, reporter);
        this.tcpTimeout = tcpTimeout;
    }

    // Visibility for testing
    TcpPing(String hostname, Integer tcpTimeout, Reporter reporter, URL url) {
        this(hostname, tcpTimeout, reporter);
        this.url = url;
    }

    @Override
    public void run() {
        HttpURLConnection con = null;
        int responseCode = -1;
        long timeAfterCall = Long.MAX_VALUE;
        long timeBeforeCall = Long.MAX_VALUE;
        String exceptionMessage = "";
        try {
            if (url == null) {
                url = new URL("http://" + hostname);
            }
            con = (HttpURLConnection) url.openConnection();
            if (con != null) {
                con.setConnectTimeout(tcpTimeout);
                con.setReadTimeout(tcpTimeout);
                timeBeforeCall = System.currentTimeMillis();
                con.connect();
                responseCode = con.getResponseCode();
                timeAfterCall = System.currentTimeMillis();
                con.disconnect();
            }
        } catch (IOException e) {
            exceptionMessage = e.getMessage();
            log.warning("Exception: " + e.getMessage());
        }
        if (!"".equals(exceptionMessage)) {
            lastResult = "Exception: " + exceptionMessage;
            updateState(hostname, MonitoringType.TCP);
            logAndReport("I/O Exception");
        } else {
            lastResult = "URL: " + url + ", round time: " + (timeAfterCall - timeBeforeCall) + " ms, status code was " + responseCode;
            updateState(hostname, MonitoringType.TCP);
        }
    }

    private void logAndReport(String whatHappened) {
        log.warning(whatHappened + " happened during TCP ping on " + hostname);
        reporter.report(hostname);
    }
}
