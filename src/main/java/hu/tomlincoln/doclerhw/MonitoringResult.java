package hu.tomlincoln.doclerhw;

import java.time.LocalDateTime;

public class MonitoringResult {

    private final String lastResult;
    private final LocalDateTime lastResultUpdated;

    public MonitoringResult(String lastResult, LocalDateTime lastResultUpdated) {
        this.lastResult = lastResult;
        this.lastResultUpdated = lastResultUpdated;
    }

    public String getLastResult() {
        return lastResult;
    }


    public LocalDateTime getLastResultUpdated() {
        return lastResultUpdated;
    }

}
