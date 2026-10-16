package movieticketbooking;

import java.util.List;

public class Reservation {
    private final List<String> seats;
    private final Showtime showtime;
    private final String id;

    public Reservation(String id, Showtime showtime, List<String> seats) {
        this.id = id;
        this.seats = seats;
        this.showtime = showtime;
    }

    public List<String> getSeats() {
        return seats;
    }

    public String getId() {
        return id;
    }

    public Showtime getShowtime() {
        return showtime;
    }
}
