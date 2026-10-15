package parkinglot;

import java.time.Duration;
import java.time.Instant;

public class HourlyPricingStrategy implements PricingStrategy{

    private final long hourlyRateCents;

    public HourlyPricingStrategy(long hourlyRateCents) {
        this.hourlyRateCents = hourlyRateCents;
    }

    @Override
    public long computeFee(Ticket ticket, Instant exitTime) {
        //get entry time from ticket and calculate duration
        Instant entryTime = ticket.entryTime();
        long durationMillis = Duration.between(entryTime, exitTime).toMillis();
        long hoursMillis = Duration.ofHours(1).toMillis(); //Get milliseconds in 1 hour
        // suppose millis in 1 hr is 10, and our duration is 11, then we need to round to 2
        //why hoursmllis - 1 ?  because  11 + 9 / 10 = 2
        // Also handles 10 + 9 / 10 = 1
        long durationHours = Math.max(1, (durationMillis + hoursMillis - 1) / hoursMillis);
        return durationHours * hourlyRateCents;
    }
}
