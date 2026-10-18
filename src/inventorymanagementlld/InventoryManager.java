package inventorymanagementlld;

import java.util.Map;

public class InventoryManager {
        private final Map<String, Warehouse> warehouses;


    public InventoryManager(Map<String, Warehouse> warehouses) {
        this.warehouses = warehouses;
    }

    public boolean addStock(String warehouseId, String productId, int quantity){
        if(warehouseId == null || warehouseId.isEmpty()){
            throw new IllegalArgumentException("warehouse cannot be empty");
        }
        if(productId == null || productId.isEmpty()){
            throw new IllegalArgumentException("product cannot be empty");
        }
        if(quantity <= 0){
            throw new IllegalArgumentException("quantity is invalid");
        }
        Warehouse warehouse = warehouses.get(warehouseId);
        if(warehouse == null){
            throw new IllegalArgumentException("warehouse is invalid");
        }

        return warehouse.addStock(productId, quantity);
    }

    public boolean removeStock(String warehouseId, String productId, int quantity){
        if(warehouseId == null || warehouseId.isEmpty()){
            throw new IllegalArgumentException("warehouse cannot be empty");
        }
        if(productId == null || productId.isEmpty()){
            throw new IllegalArgumentException("product cannot be empty");
        }
        if(quantity <= 0){
            throw new IllegalArgumentException("quantity is invalid");
        }
        Warehouse warehouse = warehouses.get(warehouseId);
        if(warehouse == null){
            throw new IllegalArgumentException("warehouse is invalid");
        }

        return warehouse.removeStock(productId, quantity);
    }

    public boolean transferStock(String fromwarehouseId, String towarehouseId, String productId, int quantity){
        return true;
    }
}