package model.response;

import java.util.Iterator;
import java.util.List;

import model.Utils;

public class FinalSummaryForOrder {
    private final double totalItemsPrice;
    private final double totalDeliveryPrice;
    private final double totalPrice;
    List<StoreSummaryForOrder> orderIncludedStoresDetails;

    public FinalSummaryForOrder (double totalItemsPrice,
                                 double totalDeliveryPrice,
                                 double totalPrice,
                                 List<StoreSummaryForOrder> orderIncludedStoresDetails) {
        this.totalItemsPrice = totalItemsPrice;
        this.totalDeliveryPrice = totalDeliveryPrice;
        this.totalPrice = totalPrice;
        this.orderIncludedStoresDetails = orderIncludedStoresDetails;
    }

    public List<StoreSummaryForOrder> getOrderIncludedStoresDetails () {
        return orderIncludedStoresDetails;
    }

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder("Final order summary:\n");
        Iterator<StoreSummaryForOrder> iterator = orderIncludedStoresDetails.iterator();
        while (iterator.hasNext()) {
            StoreSummaryForOrder storeSummaryForOrder = iterator.next();
            builder.append("\n{" + storeSummaryForOrder + "}");
            if (iterator.hasNext()) {
                builder.append(",\n");
            }
        }
        builder.append("\n\n")
               .append("totalItemsPrice= ")
               .append(Utils.round(totalItemsPrice, 2))
               .append(",\n totalDeliveryPrice= ")
               .append(Utils.round(totalDeliveryPrice, 2))
               .append(",\n totalPrice= ")
               .append(Utils.round(totalPrice, 2));

        return builder.toString();
    }
}
