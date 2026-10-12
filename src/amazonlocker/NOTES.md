# AMAZON LOCKER
# Carrier should be able to deposit package(s) in the locker.
# User should be able to pickup package from the locker.

## Clarifying questions & answers
1. Are there any different sized components? - yes, sizes - SMALL, MEDIUM, LARGE
2. Can we deposit a smaller package into a 
large compartment if all the smaller compartments are full? - same size mapping for now
3. Can a compartment store multiple packages if the user has multiple packages? - 1 : 1 
4. How will the user receive the pick up code, any SMS service? - Scope is only code generation,
delivery of code is out of scope, some downstream will handle.
5. Is there any time limit for the compartment to hold the package? - hold for 7 days
6. What happens to the packages that are not picked up in the given time? - remove and free compartment
7. What should happen in case all the compartments are occupied? - return error

## Requirements
1. Carrier deposits package by specifying size - SMALL, MEDIUM, LARGE
    - returns access code after successful deposit
    - error if no available compartment
2. User retrieves package by specifying access code
    - validate code and open compartment
    - throw error if invalid code
3. Access codes expire after 7 days
    - system opens compartments with expired codes
    - operator manually clears packages and frees compartments

## Out of scope:
- How the package gets to the locker (delivery logistics)
- How the access token reaches the customer (SMS/email notification)
- Lockout after failed access token attempts
- UI/rendering layer
- Multiple locker stations
- Payment or pricing

## Entities & relationships
Locker
Compartments
Access tokens - because there's expiration

## Class design 

## APIs
- String depositPackage(CompartmentSize size)
- void/error pickupPackage(String code)
- void cleanupExpiredAccess()


## Design patterns used (and why)

## Trade-offs / what I'd do with more time
