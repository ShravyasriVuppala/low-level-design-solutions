package parkinglot;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ParkingLotDemo {
    public static void main(String[] args) {
        HourlyPricingStrategy pricing = new HourlyPricingStrategy(200);
        ParkingLot lot = new ParkingLot(List.of(
                new ParkingSpot("M1", SpotType.MOTORCYCLE),
                new ParkingSpot("C1", SpotType.CAR),
                new ParkingSpot("L1", SpotType.LARGE)
        ), pricing);

        System.out.println("1. Park one vehicle of each type ($2 per started hour)");
        Ticket motorcycle = lot.enter(VehicleType.MOTORCYCLE);
        Ticket car = lot.enter(VehicleType.CAR);
        Ticket large = lot.enter(VehicleType.LARGE);
        System.out.println(motorcycle);
        System.out.println(car);
        System.out.println(large);

        System.out.println("\n2. Reject another car when its spot is occupied");
        attempt(() -> lot.enter(VehicleType.CAR));

        System.out.println("\n3. Exit and release the car spot");
        System.out.println("Fee: " + lot.exit(car.ticketID()) + " cents");

        System.out.println("\n4. A new car can use the released spot");
        Ticket nextCar = lot.enter(VehicleType.CAR);
        System.out.println("Assigned spot: " + nextCar.SpotID());

        System.out.println("\n5. Reject reused and invalid tickets");
        attempt(() -> lot.exit(car.ticketID()));
        attempt(() -> lot.exit("unknown-ticket"));
        attempt(() -> lot.exit(""));
        attempt(() -> lot.exit(null));

        System.out.println("\n6. Demonstrate hourly rounding without waiting");
        Instant entry = Instant.parse("2026-01-01T10:00:00Z");
        Ticket example = new Ticket("example", "C1", VehicleType.CAR, entry);
        System.out.println("Exactly 1 hour: "
                + pricing.computeFee(example, entry.plus(1, ChronoUnit.HOURS)) + " cents");
        System.out.println("61 minutes: "
                + pricing.computeFee(example, entry.plus(61, ChronoUnit.MINUTES)) + " cents");

        System.out.println("\n7. Remaining vehicles exit");
        System.out.println("Motorcycle fee: " + lot.exit(motorcycle.ticketID()) + " cents");
        System.out.println("Large vehicle fee: " + lot.exit(large.ticketID()) + " cents");
        System.out.println("New car fee: " + lot.exit(nextCar.ticketID()) + " cents");
    }

    private static void attempt(Runnable action) {
        try {
            action.run();
            throw new AssertionError("Expected request to be rejected");
        } catch (RuntimeException e) {
            System.out.println("Rejected: " + e.getMessage());
        }
    }
}
