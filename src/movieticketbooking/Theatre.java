package movieticketbooking;

import java.util.List;

public class Theatre {
    private final List<Showtime> showtimes;
    private final String name;

    public Theatre(List<Showtime> showtimes, String name) {
        this.showtimes = showtimes;
        this.name = name;
    }

    List<Showtime> getShowtimes(){
        return showtimes;
    }

    public String getName() {
        return name;
    }
}
