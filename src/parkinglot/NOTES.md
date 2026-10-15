# Parking lot

## Clarifying questions & answers
- Are we building a generic parking lot or for any specific org?
- Is it a multi-level parking? - yes
- Different types of parking spots? - small, medium, large
- Is it charged? Should it support multiple payment types? - yes, cash/card
- Will it be charged based on hours or any other strategy? - hourly basis 
- prepaid or postpaid? - postpaid
- Should it be extensible to create more space/floor in future?
- Multiple entry/exit? - yes
- Parking strategy? - Spot nearest to entry gate should be assigned
- parking strategy differs with gate? - if yes then strategy will be associated with entrygate
- Are there going to be different parking strategies? 
- concurrency? - yes

## Requirements
1. System supports three vehicle types: Motorcycle, Car, Large Vehicle
2. When a vehicle enters, system automatically assigns an available compatible spot
3. System issues a ticket at entry.
4. When a vehicle exits, user provides ticket ID
    - System validates the ticket
    - Calculates fee based on time spent (hourly, rounded up)
    - Frees the spot for next use
5. Pricing is hourly with same rate for all vehicles
6. System rejects entry if no compatible spot is available
7. System rejects exit if ticket is invalid or already used

## Out of SCope
- Payment processing
- Physical gate hardware
- Security cameras or monitoring
- UI/display systems
- Reservations or pre-booking

## Entities & relationships
ParkingLot
ParkingSpot
Ticket

Vehicle - not added because we dont need Vehicle info, we just need the type of vehicle
to park it

## Class design

## APIs

- Ticket/error enter(vehicleType)
- long/error exit(ticketId) //returns fee to pay

## Design patterns used (and why)

## Trade-offs / what I'd do with more time
