package parkinglot;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class ParkingLot {

    private final List<ParkingSpot> spots;
    private final Set<String> occupiedSpotIds;
    private final Map<String, Ticket> activeTickets;
    // private final long hourlyRateCents;
    //Inject pricing strategy into parking lot via construction
    private final PricingStrategy pricingStrategy;

    public ParkingLot(List<ParkingSpot> spots, PricingStrategy pricingStrategy) {
        this.spots = spots;
        this.pricingStrategy = pricingStrategy;
        this.occupiedSpotIds = new HashSet<>();
        this.activeTickets = new HashMap<>();
    }

    String findAvailableSpot(VehicleType vehicleType){
        //iterate on all spots, check type and not in occupied spots
        for(ParkingSpot spot : spots){
            SpotType requiredSpotType = mapVehicleTypeToSpotType(vehicleType);
            String spotId = spot.getSpotId();
            if(!occupiedSpotIds.contains(spotId) && spot.getSpotType() == requiredSpotType){
                return spotId; //O(1) lookup to check if occupied
            }
        }
        return null;
    }

    private SpotType mapVehicleTypeToSpotType(VehicleType vehicleType) {
        if(vehicleType == VehicleType.MOTORCYCLE) return SpotType.MOTORCYCLE;
        if(vehicleType == VehicleType.CAR) return SpotType.CAR;
        if(vehicleType == VehicleType.LARGE) return SpotType.LARGE;
        throw new RuntimeException("Unknown vehicle type");
    }

    //entry - find available parking spot and return ticket ID / error if couldnt find spot
    public synchronized Ticket enter(VehicleType vehicleType){
        //find available suitable spots
        String spotId = findAvailableSpot(vehicleType);
        if(spotId == null){
            throw new IllegalStateException("No available spot");
        }
        //create ticket and add spotId to occupied set
        String ticketId = UUID.randomUUID().toString();
        Ticket ticket = new Ticket(ticketId, spotId, vehicleType, Instant.now());
        activeTickets.put(ticketId, ticket);
        occupiedSpotIds.add(spotId);
        return ticket;
    }

    //exit - validate ticket and return fee
    public synchronized long exit(String ticketId){
        //validate ticketId
        if(ticketId == null || ticketId.isEmpty()){
            throw new RuntimeException("Invalid Ticket Id");
        }

        Ticket ticket = activeTickets.get(ticketId);
        if(ticket == null){
            throw new RuntimeException("Ticket not found or already used");
        }

        //calculate fee
        long fee = pricingStrategy.computeFee(ticket, Instant.now());
        //Returns fee to satisfy base requirements, can add payment here if requirements expect

        occupiedSpotIds.remove(ticket.SpotID());
        activeTickets.remove(ticketId);

        return fee;
    }

//    long computeFee(Instant entryTime, Instant exitTime){
//        long durationMillis = Duration.between(entryTime, exitTime).toMillis();
//        long hoursMillis = Duration.ofHours(1).toMillis();
//        //eg: if duration is over rounded hour, adding hour-1 rounds to next hour.
//        //But for exact hour, it doesnt round to next hour.
//        // 11/10 gives 1 but we need 2, so 11+9/10 gives 2. But for 10 it stays 10/10 = 1
//        long billedHours = Math.max(1, (durationMillis + hoursMillis - 1) / hoursMillis);
//
//        return billedHours * hourlyRateCents;
//    }
}
