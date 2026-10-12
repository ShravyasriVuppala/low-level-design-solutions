package amazonlocker;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Locker {
    private final Compartment[] compartments;
    private final Map<String, AccessToken> codeRegistry;
    private final UUID lockerId;

    public Locker(Compartment[] compartments){
        this.lockerId = UUID.randomUUID();
        this.compartments = compartments;
        codeRegistry = new HashMap<>();
    }
    //deposits package
    public String depositPackage(CompartmentSize size){
        //get available compartment with given size
        //opens compartment
        //generates access code
        //mark compartment as occupied
        //set expiry on the access token
        //add to registry
        Compartment compartment = getAvailableCompartment(size);
        if(compartment == null){
            throw new NoSuchElementException("Compartment not available");
        }
        compartment.markOccupied();
        String code = UUID.randomUUID().toString();
        Date expiry = Date.from(Instant.now().plus(Duration.ofDays(7)));
        AccessToken token = new AccessToken(code, expiry, compartment);
        codeRegistry.put(code, token);
        return code;
    }

    public void retrievePackage(String code){
        //Validate null code
        // Validate code exists
        //Get Compartment, open and mark available
        //remove code from registry
        Objects.requireNonNull(code, "Code cannot be null");

        if(code.isBlank() || !codeRegistry.containsKey(code)){
            throw new IllegalArgumentException("Invalid code entered");
        }
        AccessToken token = codeRegistry.remove(code);
        token.getCompartment().markAvailable();
    }

    private Compartment getAvailableCompartment(CompartmentSize size){
        //get first available matching compartment
        for(Compartment compartment : compartments){
            if(size == compartment.getSize() && compartment.isAvailable())
                return compartment;
        }
        return null;
    }

    public void cleanupExpiredPackages(){
        //get expired packages
        //open and free compartments associated with expired packages
        //remove codes from registry
        Date now = Date.from(Instant.now());
        codeRegistry.values().removeIf(token -> {
            if(!token.getExpirationDate().before(now)){
                return false;
            }
            token.getCompartment().markAvailable();
            return true;
        });
    }

    public UUID getLockerId() {
        return lockerId;
    }
}
