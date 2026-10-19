package loggingservice;

public class PlainTextFormatter implements Formatter{

    @Override
    public String format(LogRecord record) {
        return record.timestamp() + " ["
                + record.threadName() + "] ["
                + record.level() + "] "
                + record.message();
    }
}
