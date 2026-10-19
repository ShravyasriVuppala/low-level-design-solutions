package amazonlocker;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/*
    Requirements
    1. Carrier deposits packages into compartments of varying sizes
    2. user gets access codes to retrieve the packages
    3. Access codes expire after 7 days of deposit
    4. Staff cleans up expired packages and frees compartments

    Entities
    AmazonLocker - orchestrator List<Compartments>, Map<Code, AccessToken>
    Compartment - size, status
    AccessToken - code, expirationDate, Compartment
    CompartmentStatus - AVAILABLE, OCCUPIED
    COMPARTMENTSIZE - SMALL, MEDIUM, LARGE
    PackageSize - SMALL, MEDIUM, LARGE
 */
public class AmazonLocker {
    private final List<Compartment> compartments;
    private final Map<String, AccessToken> accessTokens;

    public AmazonLocker(List<Compartment> compartments) {
        this.compartments = compartments;
        this.accessTokens = new HashMap<>();
    }

    public String depositPackage(CompartmentSize size){
        //get available compartments
        //create access token and return code
        Compartment availableCompartment = null;
        for(Compartment compartment : compartments){
            if(size == compartment.getSize() && compartment.isAvailable()){
                availableCompartment = compartment;
            }
        }
        if(availableCompartment == null){
            throw new NoSuchElementException("Compartment not available");
        }
        String code = UUID.randomUUID().toString();
        Date expiry = Date.from(Instant.now().plus(Duration.ofDays(7)));
        availableCompartment.markOccupied();
        AccessToken token = new AccessToken(code, availableCompartment, expiry);
        accessTokens.put(code, token);
        return code;
    }
    public void retrievePackage(String code){
        //get the token from code
        //check if expired
        //free compartment and remove from the map
        AccessToken token = accessTokens.get(code);
        token.getCompartment().markFree();
        accessTokens.remove(code);
        return;
    }
}

public class Compartment{
    private final CompartmentSize size;
    private CompartmentStatus status;

    public Compartment(CompartmentSize size, CompartmentStatus status) {
        this.size = size;
        this.status = CompartmentStatus.AVAILABLE;
    }
    public CompartmentSize getSize(){
        return size;
    }
    public void markFree(){
        status = CompartmentStatus.AVAILABLE;
    }
    public boolean isAvailable(){
        return status == CompartmentStatus.AVAILABLE;
    }

    public void markOccupied(){
        status = CompartmentStatus.OCCUPIED;
    }

}
public class AccessToken{
    String code;
    Date ExpirationDate;
    Compartment compartment;

    public AccessToken(String code, Compartment compartment, Date expirationDate) {
        this.code = code;
        this.compartment = compartment;
        ExpirationDate = expirationDate;
    }

    public Compartment getCompartment() {
        return compartment;
    }
}

public enum CompartmentSize{
    SMALL, MEDIUM, LARGE
}
public enum CompartmentStatus{
    AVAILABLE, OCCUPIED
}
