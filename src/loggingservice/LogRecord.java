package loggingservice;

import java.time.Instant;

public record LogRecord (
        LogLevel level,
        String message,
        Instant timestamp,
        String threadName
){

}
