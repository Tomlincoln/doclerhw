package hu.tomlincoln.doclerhw;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class IcmpPing extends MonitoringItem {

    public static final String EXTRA_ARGUMENTS = "-n 5";
    public static final String DNS_FAILURE = "Ping request could not find host ";
    public static final String ALL_OK_AFTER_5_PING = "    Packets: Sent = 5, Received = 5, Lost = 0 (0% loss),";
    private static final Logger log = Logger.getLogger("");
    private final String command;

    public IcmpPing(String command, String hostname, Reporter reporter) {
        super(hostname, reporter);
        this.command = command;
    }

    @Override
    public void run() {
        StringBuilder builder = new StringBuilder();
        boolean isOnError = false;
        try {
            Runtime rt = Runtime.getRuntime();
            Process pr = rt.exec(command + " " + EXTRA_ARGUMENTS + " " + hostname);
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String line;
            int lineCounter = 0;
            while((line = input.readLine()) != null) {
                lineCounter++;
                boolean dnsError = lineCounter == 1 && line.startsWith(DNS_FAILURE + hostname);
                boolean anyOtherError = lineCounter == 10 && !line.equals(ALL_OK_AFTER_5_PING);
                if (dnsError || anyOtherError) {
                    isOnError = true;
                }
                builder.append(line).append("\n");
                this.lastResult = builder.toString();
            }
            updateState(hostname, MonitoringType.ICMP);
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
