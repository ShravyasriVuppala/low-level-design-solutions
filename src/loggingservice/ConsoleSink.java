package loggingservice;

public class ConsoleSink implements Sink{
    @Override
    public void write(String message) {
        System.out.println(message);
    }
}
