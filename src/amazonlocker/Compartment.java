package amazonlocker;

public class Compartment {
    private final CompartmentSize size;
    private CompartmentStatus status;

    public Compartment(CompartmentSize size) {
        this.size = size;
    }

    public CompartmentStatus getStatus() {
        return status;
    }

    public CompartmentSize getSize() {
        return size;
    }

    public boolean isAvailable(){
        return getStatus() == CompartmentStatus.AVAILABLE;
    }


    public void markOccupied(){
        setStatus(CompartmentStatus.OCCUPIED);
    }

    public void markAvailable(){
        setStatus(CompartmentStatus.AVAILABLE);
    }

    public void setStatus(CompartmentStatus status) {
        this.status = status;
    }
}
