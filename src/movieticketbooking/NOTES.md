# Movie ticket booking

## Clarifying questions & answers
- When you say 'browse movies,' is that full-text search, fuzzy matching, 
or just simple title lookup? - simple text matching
- How does seat selection work? Does the user pick specific seats from a map, 
or does the system auto-assign? And can they book more than one seat at a time? - seat selection
- Are we designing for a single theater or multiple? And do theaters have multiple screens?
- Are we designing for a single theater or multiple? And do theaters have multiple screens?
  Multiple theaters, each with multiple screens. A user can search for a movie and 
see where it's playing, or go to a specific theater and see what's on.
- Do different screens have different seat configurations? Or can we standardize? 
Standard - A through Z, seats 0 through 20.
- What does 'manage reservations' include? Cancel, reschedule, modify? - cancel
- Different Seat types? Payment in scope? - no
- Concurrency is core

## Requirements
1. Users can search for movies by title
2. Users can browse movies playing at a given theater
3. Theaters have multiple screens; all screens share the same seat layout (rows A-Z, seats 0-20)
4. Users can view available seats for a showtime and select specific ones
5. Users can book multiple seats in a single reservation; booking returns a confirmation ID
6. Concurrent booking of the same seat: exactly one succeeds
7. Users can cancel a reservation by confirmation ID, releasing the seats

## Out of Scope
- Payment processing (assume payment always succeeds)
- Variable seat layouts or seat types (all seats identical)
- Rescheduling (cancel and rebook instead)
- UI / rendering

## Entities & relationships
Movie
Theatre
Showtime 
Screen - String (as seat layout same for all screens)
Seat - String (as no differentiation btwn seats)
Reservation
BookingSystem

BookingSystem → List<Theater>
Theater → List<Showtime>
Showtime → Theater (back-reference for navigation)
Showtime → Movie (reference)
Showtime → List<Reservation> (booking records for this showtime)
Reservation → Showtime (back-reference for cancellation routing)
Reservation → List<string> (e.g., ["A5", "A6"])

## Class design

# BookingSystem
    - List<Theatre>
    - searchMovies(String movieTitle)
    - getShowtimesAtTheater(String theatre) -> List<Showtime>
    - book(showtime, seats)
    - cancelReservation(String confirmationId)
    
# Theatre
    - List<ShowTime>
# ShowTime
    - String[] AvailableSeats
    - Theatre
    - Movie
# Reservation
    - Showtime
    - String[] seats
    - confirmationId

## APIs
- searchMovies(String movieTitle) -> List<ShowTime>
- getShowtimesAtTheater(String theatre) -> List<Showtime>
- book(showtime, seats) -> String confirmationId
- cancelReservation(String confirmationId) -> boolean

## Design patterns used (and why)

## Trade-offs / what I'd do with more time
