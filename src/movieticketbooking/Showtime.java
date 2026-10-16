package movieticketbooking;

import java.time.Instant;
import java.util.*;

public class Showtime {
    private static final Set<String> SEAT_LAYOUT = createSeatLayout();
    private final String id;
    private final Theatre theatre;
    private final Movie movie;
    private final Instant showStartTime;
    private Set<Reservation> reservations;
    private final Set<String> bookedSeats = new HashSet<>();


    public Showtime(Theatre theatre, Movie movie, Instant showStartTime) {
        this.showStartTime = showStartTime;
        this.id = UUID.randomUUID().toString();
        this.theatre = theatre;
        this.movie = movie;
        reservations = new HashSet<>();
    }

    public String getId() {
        return id;
    }

    public Theatre getTheatre() {
        return theatre;
    }

    public Movie getMovie() {
        return movie;
    }

    public Instant getShowStartTime() {
        return showStartTime;
    }

    public Set<Reservation> getReservations() {
        return reservations;
    }

    public synchronized Reservation book(Reservation reservation) {
        List<String> seatIds = reservation.getSeats();
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Select at least one seat");
        }

        Set<String> requestedSeats = new LinkedHashSet<>(seatIds);
        if (requestedSeats.size() != seatIds.size()) {
            throw new IllegalArgumentException("Duplicate seat selected");
        }

        for (String seatId : requestedSeats) {
            if (!SEAT_LAYOUT.contains(seatId)) {
                throw new IllegalArgumentException("Invalid seat: " + seatId);
            }
            if (bookedSeats.contains(seatId)) {
                throw new IllegalStateException("Seat unavailable: " + seatId);
            }
        }

        bookedSeats.addAll(requestedSeats);
        reservations.add(reservation);
        return reservation;
    }

    public synchronized void cancel(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation not found");
        }

        bookedSeats.removeAll(reservation.getSeats());
    }

    public synchronized List<String> getAvailableSeats() {
        List<String> available = new ArrayList<>();
        for (String seatId : SEAT_LAYOUT) {
            if (!bookedSeats.contains(seatId)) {
                available.add(seatId);
            }
        }
        return available;
    }

    public synchronized boolean isAvailable(String seatId) {
        return SEAT_LAYOUT.contains(seatId) && !bookedSeats.contains(seatId);
    }

    private static Set<String> createSeatLayout() {
        Set<String> seats = new LinkedHashSet<>();
        for (char row = 'A'; row <= 'Z'; row++) {
            for (int number = 0; number <= 20; number++) {
                seats.add("" + row + number);
            }
        }
        return Collections.unmodifiableSet(seats);
    }
}
