package parkinglot;

import java.time.Instant;

public interface PricingStrategy {
    long computeFee(Ticket ticket, Instant exitTime);
}
