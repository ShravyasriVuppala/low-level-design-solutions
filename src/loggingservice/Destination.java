package loggingservice;

public class Destination{
    private final LogLevel minThreshold;
    private final Formatter format;
    private final Sink sink;

    public Destination(LogLevel minThreshold, Formatter format, Sink sink) {
        this.minThreshold = minThreshold;
        this.format = format;
        this.sink = sink;
    }

    public void write(LogRecord record){
        //filters
        //format
        // write to sink
        if(!record.level().isAtleast(minThreshold)){
            return;
        }

        String formatted = format.format(record);

        synchronized (this){
            try{
               sink.write(formatted);
            }
            catch (Exception e){
                System.err.println("logger: sink write failed: " + e.getMessage());
            }
        }
    }

}
