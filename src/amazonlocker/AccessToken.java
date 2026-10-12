package amazonlocker;

import java.time.Instant;
import java.util.Date;

public class AccessToken {
    private final String code;
    private final Date expirationDate;
    private final Compartment compartment;

    public AccessToken(String code, Date expirationDate, Compartment compartment){
        this.code = code;
        this.compartment = compartment;
        this.expirationDate = expirationDate;
    }

    public Compartment getCompartment() {
        return compartment;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public String getCode() {
        return code;
    }
}
