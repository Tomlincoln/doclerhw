package hu.tomlincoln.doclerhw;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class PingApplication {

    private static final Logger log = Logger.getLogger("");
    private final Properties properties;
    private final Reporter reporter;

    public PingApplication(Properties properties, Reporter reporter) {
        this.properties = properties;
        if (this.properties.isEmpty()) {
            loadPropertiesFromFile();
        }
        if (this.properties.isEmpty()) {
            loadPropertiesFromResources();
        }
        if (reporter == null) {
            this.reporter = new Reporter(properties.getProperty("report.url"));
        } else {
            this.reporter = reporter;
        }
        setupLoggingWithFile();
    }

    public void start() {
        String[] hostnames = properties.getProperty("app.hosts").split(",");
        String icmpCommand = properties.getProperty("icmp.command");
        int icmpDelay = Integer.parseInt(properties.getProperty("icmp.delay"));
        int tcpDelay = Integer.parseInt(properties.getProperty("tcp.delay"));
        int tcpTimeout = Integer.parseInt(properties.getProperty("tcp.timeout"));
        String traceCommand = properties.getProperty("trace.command");
        int traceDelay = Integer.parseInt(properties.getProperty("trace.delay"));

        for (String hostname : hostnames) {
            startThreadsForHost(hostnames.length, icmpCommand, icmpDelay, hostname);
            startThreadsForHost(hostnames.length, traceCommand, traceDelay, hostname);
            startThreadsForHostWithTcpTimeout(hostnames.length, "", tcpDelay, hostname, tcpTimeout);
        }
    }

    private void setupLoggingWithFile() {
        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler(properties.getProperty("log.location"), true);
        } catch (IOException e) {
            log.severe("I/O Exception occurred: " + e.getMessage());
            throw new Error("Cannot open log file, exiting...");
        }
        fileHandler.setFormatter(new SimpleFormatter() {
            @Override
            public synchronized String format(LogRecord logRecord) {
                return String.format("[%1$tF %1$tT] [%2$-7s] %3$s %n", new Date(logRecord.getMillis()), logRecord.getLevel().toString(), logRecord.getMessage());
            }
        });
        fileHandler.setLevel(Level.WARNING);
        //log.removeHandler(log.getHandlers()[0]);
        log.setLevel(Level.FINE);
        log.addHandler(fileHandler);
    }

    private void startThreadsForHost(int threadCount, String command, int delay, String hostname) {
        startThreadsForHostWithTcpTimeout(threadCount, command, delay, hostname, 0);
    }

    private void startThreadsForHostWithTcpTimeout(int threadCount, String command, int delay, String hostname, int tcpTimeout) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(threadCount);
        Runnable runnable;
        if (command.contains("ping")) {
            runnable = new IcmpPing(command, hostname, reporter);
        } else if (command.contains("trace")) {
            runnable = new TraceRoute(command, hostname, reporter);
        } else {
            runnable = new TcpPing(hostname, tcpTimeout, reporter);
        }
        executor.scheduleAtFixedRate(runnable, 0, delay, TimeUnit.SECONDS);
    }

    private void loadPropertiesFromResources() {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream input = classLoader.getResourceAsStream("app.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            log.severe("I/O Exception occurred during loading properties file from resource: " + ex.getMessage());
            throw new Error("Cannot load properties file, exiting...");
        }
    }

    private void loadPropertiesFromFile() {
        try {
            FileInputStream file = new FileInputStream("./main.properties");
            properties.load(file);
            file.close();
        } catch (IOException ex) {
            log.fine("I/O Exception occurred during loading properties file from external file: " + ex.getMessage());
            log.fine("Trying to load properties from resources then...");
        }
    }
}
