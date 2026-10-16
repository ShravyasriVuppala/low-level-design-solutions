package movieticketbooking;

import java.time.Instant;
import java.util.*;

public class BookingSystem {
    private final List<Theatre> theatres;
    private final Map<String, Movie> moviesById; // index used to search optimally
    private final Map<String, List<Showtime>> showtimesByMovieId; // index used to search optimally
    private final Map<String, Showtime> showtimeById; // index used to book by showtimeId
    private final Map<String, Reservation> reservationsById;
    public BookingSystem(List<Theatre> theatres) {
        this.theatres = theatres;
        this.moviesById = new HashMap<>();
        this.showtimesByMovieId = new HashMap<>();
        this.showtimeById = new HashMap<>();
        this.reservationsById = new HashMap<>();
        //populate indexes
        for(Theatre theatre : theatres){
            for(Showtime showtime : theatre.getShowtimes()){
                String showtimeId = showtime.getId();
                Movie movie = showtime.getMovie();
                String movieId = movie.getId();
                moviesById.put(movieId, movie);
                showtimeById.put(showtimeId, showtime);
                showtimesByMovieId
                        .computeIfAbsent(movieId, v -> new ArrayList<>())
                        .add(showtime);
            }
        }
    }

    //search movies by title - return movies with respective showtimes
    public List<Showtime> searchMoviesByTitle(String title){
        //iterate on movies index and return show times for that movie
        List<Showtime> result = new ArrayList<>();
        String titleLower = title.toLowerCase();
        Instant now = Instant.now();
        for(Movie movie : moviesById.values()){
            if(movie.getTitle().toLowerCase().contains(titleLower)){
                //get showtimes and filter future showtimes
                List<Showtime> showtimes = showtimesByMovieId.get(movie.getId());
                if(showtimes != null){
                    for(Showtime show : showtimes){
                        if(show.getShowStartTime().isAfter(now)){
                            result.add(show);
                        }
                    }
                }
            }
        }
        return result;
    }

    // search movies by theatre - return movies streaming in theatre with respective showtimes
    public List<Showtime> getShowtimesAtTheater(Theatre theatre){
        //get theatre and prepare showtimes list
        List<Showtime> result = new ArrayList<>();
        for(Showtime show : theatre.getShowtimes()){
            if(show.getShowStartTime().isAfter(Instant.now())){
                result.add(show);
            }
        }
        return result;
    }

    // create booking
    public Reservation book(String showtimeId, List<String> seats){
        /**
         * Get showtime object
         * Create reservation
         * tag reservation to showtime
         * add reservation to reservations map
         */
        if (showtimeId == null || seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("Invalid booking request");
        }

        Showtime showtime = showtimeById.get(showtimeId);
        if (showtime == null) {
            throw new NoSuchElementException("Showtime not found: " + showtimeId);
        }

        Reservation reservation = new Reservation(
                UUID.randomUUID().toString(),
                showtime,
                seats
        );

        showtime.book(reservation);

        reservationsById.put(reservation.getId(), reservation);

        return reservation;
    }

    //cancel booking
    public void cancelReservation(String confirmationId) {
        if (confirmationId == null || confirmationId.isEmpty()) {
            throw new IllegalArgumentException("Invalid confirmation ID");
        }

        Reservation reservation = reservationsById.get(confirmationId);
        if (reservation == null) {
            throw new NoSuchElementException("Reservation not found: " + confirmationId);
        }

        Showtime showtime = reservation.getShowtime();
        showtime.cancel(reservation);

        reservationsById.remove(confirmationId);
    }
}
