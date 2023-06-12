package hu.tomlincoln.doclerhw;

import java.time.LocalDateTime;
import java.util.logging.Logger;

public abstract class MonitoringItem implements Runnable {

    protected final String hostname;
    private static final Logger log = Logger.getLogger("");
    protected String lastResult;
    protected Reporter reporter;

    public MonitoringItem(String hostname, Reporter reporter) {
        this.reporter = reporter;
        this.hostname = hostname;
        this.lastResult = App.NO_RESULT;
    }

    protected void updateState(String hostname, MonitoringType type) {
        synchronized (App.class) {
            log.fine("Starting update state for " + hostname + " (type " + type.name() + ")");
            App.getState(hostname).put(type, new MonitoringResult(lastResult, LocalDateTime.now()));
            log.fine("State updated for " + hostname + " (type " + type.name() + ")");
        }
    }
}
