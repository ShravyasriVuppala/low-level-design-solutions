package inventorymanagementlld;

import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

public class Warehouse {
    private final Map<String, List<AlgoConfig>> alerts;
    private final Map<String, Integer> inventory;

    public Warehouse(Map<String, List<AlgoConfig>> alerts, Map<String, Integer> inventory) {
        this.alerts = alerts;
        this.inventory = inventory;
    }

    boolean addStock(String productId, int quantity){
        //get stock
        synchronized (this){
            int available = inventory.getOrDefault(productId, 0);
            inventory.put(productId, available + quantity);
        }
        return true;
    }

    boolean removeStock(String productId, int quantity){

        //get stock
        int available, remaining;
        synchronized (this){
            available = inventory.get(productId);

            if(available < quantity){
                return false;
            }
            remaining = available - quantity;
            inventory.put(productId, remaining);
        }
        setAlert(productId, available, remaining);
        return true;
    }

    boolean setAlert(String productId, int before, int after){
        return true;
    }
}
