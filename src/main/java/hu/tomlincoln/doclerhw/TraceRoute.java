package hu.tomlincoln.doclerhw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class TraceRoute extends MonitoringItem {

    public static final String DNS_FAILURE = "Unable to resolve target system name ";
    public static final String ALL_OK = "Trace complete.\n";
    private static final Logger log = Logger.getLogger("");
    private final String command;

    public TraceRoute(String command, String hostname, Reporter reporter) {
        super(hostname, reporter);
        this.command = command;
    }

    @Override
    public void run() {
        StringBuilder builder = new StringBuilder();
        boolean isOnError = false;
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(command + " " + hostname);
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            int lineCounter = 0;
            while((line = input.readLine()) != null) {
                lineCounter++;
                if (lineCounter == 1 && line.startsWith(DNS_FAILURE + hostname + ".")) {
                    isOnError = true;
                }
                builder.append(line).append("\n");
                this.lastResult = builder.toString();
            }
            if (!lastResult.endsWith(ALL_OK)) {
                isOnError = true;
            }
            updateState(hostname, MonitoringType.TRACE);
            if (isOnError) {
                logAndReport("Network error");
            }
        } catch (IOException e) {
            logAndReport("I/O exception (" + e.getMessage() + ")");
        }
    }

    private void logAndReport(String whatHappened) {
        log.warning(whatHappened + " happened during " + command + " on " + hostname);
        reporter.report(hostname);
    }
}
