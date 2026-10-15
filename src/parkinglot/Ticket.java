package parkinglot;

import java.time.Instant;

public record Ticket(
        String ticketID,
        String SpotID,
        VehicleType vehicleType,
        Instant entryTime) {
}
