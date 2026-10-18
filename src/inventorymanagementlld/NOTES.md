# Inventory management

Design an inventory management system that tracks products across multiple warehouses. 
The system needs to handle adding and removing inventory, transferring stock between 
locations, and alerting when inventory runs low.

## Clarifying questions & answers
- Who are we alerting and how? Is alert delivery in scope? - No
- Are there gonna be diff alerts for diff thresholds? - Yes
- Does this alert config differ between warehouses? - Yes
- Is this threshold same for all products in warehouse? - Yes
- what shpuld happen if no stock to fulfill request? - return error
- 

## Requirements
FR1 - Add item to warehouse
FR2 - remove item from warehouse
FR3 - transfer btwn warehouses
FR4 - alert on low stock per product per warehouse
FR5 - Reject requests when no stock 
FR6 - System should be thread-safe

## Out of SCope
- Notification delivery

## Entities & relationships
InventoryManager - Class
Warehouse - Class
Product - String/Integer
AlertConfig 

## Class design
InventoryManager
    - Warehouses Map<WarehouseId, Warehouse>

    + InventoryManager(warehouses)
    + addStock(warehouseId, productId, quantity)
    + removeStock(warehouseId, productId, quantity)
    + transferStock(fromWarehouseId, toWarehouseId, productId, quantity)

Warehouse
    - products Map<productId, quantity>
    - alerts Map<productId, alertConfig>

    + addStock(productId, quantity)
    + removeStock(productId, quantity)
    + setAlert(productId, valueBefore, valueAfter)


## APIs

## Design patterns used (and why)

## Trade-offs / what I'd do with more time

## Implementation
