package amazonlocker;

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
        Date expiry = Date.from(Instant.now().plus(7, TimeUnit.DAYS.toChronoUnit()));
        AccessToken token = new AccessToken(code, expiry, compartment);
        codeRegistry.put(code, token);
        return code;
    }

    public void retrievePackage(String code){
        //Validate null code
        // Validate code exists
        //Get Compartment, open and mark available
        if(code.isBlank()){
            throw new IllegalArgumentException("Access code cannot be empty");
        }
        if(!codeRegistry.containsKey(code)){
            throw new IllegalArgumentException("Invalid code entered");
        }
        AccessToken token = codeRegistry.get(code);
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

    public UUID getLockerId() {
        return lockerId;
    }
}
