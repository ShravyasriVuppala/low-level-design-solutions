package parkinglot;

public class ParkingSpot {
    private final String spotId;
    private final SpotType spotType;

    public ParkingSpot(String spotId, SpotType spotType) {
        this.spotId = spotId;
        this.spotType = spotType;
    }

    public String getSpotId() {
        return spotId;
    }

    public SpotType getSpotType() {
        return spotType;
    }
}
