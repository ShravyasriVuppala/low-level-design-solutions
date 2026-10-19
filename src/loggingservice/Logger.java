package loggingservice;

import java.time.Instant;
import java.util.List;

/*
    Requirements'
    1. In-process logger with severity levels - DEBUG, INFO, WARN, ERROR, FATAL
    2. Write logs into all the given destinations (set at logger initialization)
    3. Destination can its own min-level threshold and a format
    4. The System should be thread-safe

    Entities
    Logger - orchestrator
    Destination - threshold, format
    Formatter - interface with format()
    Sink - interface with write()
    LogRecord - record with level, message, timestamp, threadname

 */
public class Logger {
    private final List<Destination> destinations;

    public Logger(List<Destination> destinations) {
        this.destinations = List.copyOf(destinations);
    }

    public void log(LogLevel level, String message){
        //create logrecord
        //iterate over all destinations
        //write
        LogRecord record = new LogRecord(level, message, Instant.now(), Thread.currentThread().getName());
        for(Destination destination : destinations){
            destination.write(record);
        }
    }
    public void info(String message){
        log(LogLevel.INFO, message);
    }
    public void debug(String message){
        log(LogLevel.DEBUG, message);
    }
    public void warn(String message){
        log(LogLevel.WARN, message);
    }
}

