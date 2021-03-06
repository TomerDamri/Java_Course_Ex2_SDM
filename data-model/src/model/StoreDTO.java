package model;

import java.util.List;
import java.util.Map;

public class StoreDTO {
    private final int id;
    private final String name;
    private final int deliveryPpk;
    private final LocationDTO location;
    private final Map<Integer, StoreItemDTO> items;
    private final List<StoreOrderDTO> orders;
    private final double totalDeliveriesPayment;
    private final List<DiscountDTO> storeDiscounts;

    public StoreDTO (int id,
                     String name,
                     int deliveryPpk,
                     LocationDTO locationDTO,
                     Map<Integer, StoreItemDTO> items,
                     List<StoreOrderDTO> orders,
                     double totalDeliveriesPayment,
                     List<DiscountDTO> storeDiscounts) {
        this.id = id;
        this.name = name;
        this.deliveryPpk = deliveryPpk;
        this.location = locationDTO;
        this.items = items;
        this.orders = orders;
        this.totalDeliveriesPayment = totalDeliveriesPayment;
        this.storeDiscounts = storeDiscounts;
    }

    public List<DiscountDTO> getStoreDiscounts () {
        return storeDiscounts;
    }

    public int getId () {
        return id;
    }

    public String getName () {
        return name;
    }

    public int getDeliveryPpk () {
        return deliveryPpk;
    }

    public LocationDTO getLocation () {
        return location;
    }

    public Map<Integer, StoreItemDTO> getItems () {
        return items;
    }

    public List<StoreOrderDTO> getOrders () {
        return orders;
    }

    public double getTotalDeliveriesPayment () {
        return totalDeliveriesPayment;
    }

    @Override
    public String toString () {
        return String.format("id: %s name: %s location: %s", id, name, location.toString());
    }
}
