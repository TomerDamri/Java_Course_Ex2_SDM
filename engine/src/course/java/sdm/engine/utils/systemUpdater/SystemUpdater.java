package course.java.sdm.engine.utils.systemUpdater;

import java.util.*;

import course.java.sdm.engine.mapper.GeneratedDataMapper;
import course.java.sdm.engine.model.*;

public class SystemUpdater {

    private static SystemUpdater singletonSystemUpdater = null;
    private final static SystemUpdaterValidator SYSTEM_UPDATER_VALIDATOR = new SystemUpdaterValidator();

    private SystemUpdater () {
    }

    public static SystemUpdater getSystemUpdater () {
        if (singletonSystemUpdater == null) {
            singletonSystemUpdater = new SystemUpdater();
        }

        return singletonSystemUpdater;
    }

    public void addItemToStore (Integer itemId,
                                Integer storeId,
                                Integer itemPrice,
                                Map<Integer, SystemStore> systemStores,
                                Map<Integer, SystemItem> systemItems) {

        validateExistenceInSystem(itemId, storeId, systemStores, systemItems);

        SystemStore systemStore = systemStores.get(storeId);
        SystemItem systemItem = systemItems.get(itemId);

        SYSTEM_UPDATER_VALIDATOR.validateItemNotExistInStore(itemId, systemStore, systemItem);

        addNewItemToStore(itemId, itemPrice, systemStore, systemItem);
        updateSystemItemAfterAddingToStore(itemId, storeId, itemPrice, systemStores, systemItem);
    }

    public void updateItemPrice (Integer itemId,
                                 Integer storeId,
                                 Integer newItemPrice,
                                 Map<Integer, SystemStore> systemStores,
                                 Map<Integer, SystemItem> systemItems) {
        validateExistenceInSystem(itemId, storeId, systemStores, systemItems);

        SystemStore systemStore = systemStores.get(storeId);
        SystemItem systemItem = systemItems.get(itemId);

        SYSTEM_UPDATER_VALIDATOR.validateItemExistInStore(itemId, systemStore, systemItem);
        // update store item price
        StoreItem storeItem = systemStore.getItemIdToStoreItem().get(itemId);
        int prevPrice = storeItem.getPrice();
        storeItem.setPrice(newItemPrice);
        // update systemItem avg price
        updateSystemItemAfterUpdatingStoreItemPrice(newItemPrice, systemStores, systemItem, prevPrice);
    }

    public DeleteItemResult deleteItemFromStore (Integer itemId,
                                                 Integer storeId,
                                                 Map<Integer, SystemStore> systemStores,
                                                 Map<Integer, SystemItem> systemItems) {
        validateExistenceInSystem(itemId, storeId, systemStores, systemItems);

        SystemStore systemStore = systemStores.get(storeId);
        SystemItem systemItem = systemItems.get(itemId);

        SYSTEM_UPDATER_VALIDATOR.validateItemExistInStore(itemId, systemStore, systemItem);
        SYSTEM_UPDATER_VALIDATOR.validateItemSellsInOtherStores(systemStore, systemItem);
        SYSTEM_UPDATER_VALIDATOR.validateStoreSellsOtherItems(systemStore, systemItem);

        // remove item from store
        StoreItem removedStoreItem = systemStore.getItemIdToStoreItem().remove(itemId);
        // remove related discounts
        List<Discount> removedDiscounts = deleteRelatedDiscounts(itemId, systemStore);
        // update system item
        updateSystemItemAfterDeletingItemFromStore(systemStores, systemItem, removedStoreItem);

        return new DeleteItemResult(removedStoreItem, removedDiscounts);
    }

    private void validateExistenceInSystem (Integer itemId,
                                            Integer storeId,
                                            Map<Integer, SystemStore> systemStores,
                                            Map<Integer, SystemItem> systemItems) {
        SYSTEM_UPDATER_VALIDATOR.validateItemExistInSystem(itemId, systemItems);
        SYSTEM_UPDATER_VALIDATOR.validateStoreExistInSystem(storeId, systemStores);
    }

    private void updateSystemItemAfterAddingToStore (Integer itemId,
                                                     Integer storeId,
                                                     Integer itemPrice,
                                                     Map<Integer, SystemStore> systemStores,
                                                     SystemItem systemItem) {
        updateStoreCountAfterAddingItemToStore(systemItem);
        updateAvgPriceAfterAddingItemToStore(itemPrice, systemItem);
        updateStoreSellsInCheapestPriceAfterAddingItemToStore(itemId, storeId, itemPrice, systemStores, systemItem);
    }

    private void updateStoreSellsInCheapestPriceAfterAddingItemToStore (Integer itemId,
                                                                        Integer storeId,
                                                                        Integer itemPrice,
                                                                        Map<Integer, SystemStore> systemStores,
                                                                        SystemItem systemItem) {
        SystemStore storeSellsInCheapestPrice = systemStores.get(systemItem.getStoreSellsInCheapestPrice());
        int cheapestPrice = storeSellsInCheapestPrice.getItemIdToStoreItem().get(itemId).getPrice();

        if (cheapestPrice > itemPrice) {
            systemItem.setStoreSellsInCheapestPrice(storeId);
        }
    }

    private void updateAvgPriceAfterAddingItemToStore (Integer itemPrice, SystemItem systemItem) {
        int storesCount = systemItem.getStoresCount();
        int prevStoreCount = storesCount - 1;
        double prevAvg = systemItem.getAvgPrice();
        double newAvgPrice = (prevAvg * prevStoreCount + itemPrice) / storesCount;

        systemItem.setAvgPrice(newAvgPrice);
    }

    private void updateStoreCountAfterAddingItemToStore (SystemItem systemItem) {
        int prevStoreCount = systemItem.getStoresCount();
        systemItem.setStoresCount(prevStoreCount + 1);
    }

    private void addNewItemToStore (Integer itemId, Integer itemPrice, SystemStore systemStore, SystemItem systemItem) {
        StoreItem newStoreItem = new StoreItem(systemItem.getItem(), itemPrice);
        systemStore.getItemIdToStoreItem().put(itemId, newStoreItem);
    }

    private void updateSystemItemAfterUpdatingStoreItemPrice (Integer newItemPrice,
                                                              Map<Integer, SystemStore> systemStores,
                                                              SystemItem systemItem,
                                                              int prevPrice) {
        updateAvgPriceAfterUpdatingStoreItemPrice(newItemPrice, systemItem, prevPrice);
        updateStoreSellsInCheapestPriceAfterUpdatingStoreItemPrice(systemStores, systemItem);
    }

    private void updateStoreSellsInCheapestPriceAfterUpdatingStoreItemPrice (Map<Integer, SystemStore> systemStores,
                                                                             SystemItem systemItem) {
        Integer itemId = systemItem.getId();
        Comparator<SystemStore> storeSellsInCheapestPriceComparator = (store1,
                                                                       store2) -> Integer.compare(store1.getItemIdToStoreItem()
                                                                                                        .get(itemId)
                                                                                                        .getPrice(),
                                                                                                  store2.getItemIdToStoreItem()
                                                                                                        .get(itemId)
                                                                                                        .getPrice());

        int newStoreSellsInCheapestPriceId = systemStores.values()
                                                         .stream()
                                                         .filter(store -> store.getItemIdToStoreItem().containsKey(itemId))
                                                         .min(storeSellsInCheapestPriceComparator)
                                                         .get()
                                                         .getId();

        systemItem.setStoreSellsInCheapestPrice(newStoreSellsInCheapestPriceId);
    }

    private void updateAvgPriceAfterUpdatingStoreItemPrice (Integer newItemPrice, SystemItem systemItem, int prevPrice) {
        int storesCount = systemItem.getStoresCount();
        double prevAvg = systemItem.getAvgPrice();
        double newAvgPrice = (prevAvg * storesCount - prevPrice + newItemPrice) / storesCount;

        systemItem.setAvgPrice(newAvgPrice);
    }

    private void updateSystemItemAfterDeletingItemFromStore (Map<Integer, SystemStore> systemStores,
                                                             SystemItem systemItem,
                                                             StoreItem removedStoreItem) {
        updateStoreCountAfterDeletion(systemItem);
        // update avg price
        updateAvgPriceAfterDeletion(systemItem, removedStoreItem);
        // update store sells in cheapest price
        updateStoreSellsInCheapestPriceAfterUpdatingStoreItemPrice(systemStores, systemItem);
    }

    private int updateStoreCountAfterDeletion (SystemItem systemItem) {
        int prevStoresCount = systemItem.getStoresCount();
        systemItem.setStoresCount(prevStoresCount - 1);
        return prevStoresCount;
    }

    private void updateAvgPriceAfterDeletion (SystemItem systemItem, StoreItem removedStoreItem) {
        double prevAvgPrice = systemItem.getAvgPrice();
        int storesCount = systemItem.getStoresCount();
        int prevStoresCount = storesCount - 1;
        double newAvgPrice = (prevAvgPrice * prevStoresCount - removedStoreItem.getPrice()) / storesCount;

        systemItem.setAvgPrice(newAvgPrice);
    }

    private List<Discount> deleteRelatedDiscounts (Integer itemId, SystemStore systemStore) {
        List<Discount> removedDiscounts = null;
        Map<Integer, List<Discount>> storeDiscounts = systemStore.getStore().getStoreDiscounts();
        if (storeDiscounts != null && storeDiscounts.containsKey(itemId)) {
            removedDiscounts = storeDiscounts.remove(itemId);
        }

        Map<Integer, Set<Discount>> discountsToRemove = new HashMap<>();

        for (Map.Entry<Integer, List<Discount>> entry : storeDiscounts.entrySet()) {
            Integer key = entry.getKey();
            List<Discount> discountList = entry.getValue();
            Set<Discount> discountsToRemoveForItem = new HashSet<>();

            for (Discount discount : discountList) {
                for (Offer offer : discount.getThenYouGet().getOffers().values()) {
                    if (offer.getItemId() == itemId) {
                        discountsToRemoveForItem.add(discount);
                    }
                }
            }

            if (!discountsToRemoveForItem.isEmpty()) {
                discountsToRemove.put(key, discountsToRemoveForItem);
            }
        }

        for (Map.Entry<Integer, Set<Discount>> entry : discountsToRemove.entrySet()) {
            Integer ifYouButItemId = entry.getKey();
            Set<Discount> storeDiscountPerItemToRemove = entry.getValue();
            List<Discount> storeDiscountPerItem = storeDiscounts.get(ifYouButItemId);

            for (Discount discount : storeDiscountPerItemToRemove) {
                if (storeDiscountPerItem.contains(discount)) {
                    if (removedDiscounts == null) {
                        removedDiscounts = new LinkedList<>();
                    }

                    removedDiscounts.add(discount);
                    storeDiscountPerItem.remove(discount);
                    if (storeDiscountPerItem.size() == 0) {
                        storeDiscounts.remove(ifYouButItemId);
                    }
                }
            }
        }

        return removedDiscounts;
    }

    public void updateSystemAfterLoadingOrdersHistoryFromFile (Map<UUID, List<SystemOrder>> ordersFromHistoryFile, Descriptor descriptor) {
        Map<UUID, List<SystemOrder>> systemOrdersBeforeUpdate = descriptor.getSystemOrders();
        Map<Integer, SystemStore> systemStores = descriptor.getSystemStores();

        updateSystemOrdersAccordingToHistoryFile(ordersFromHistoryFile, descriptor, systemOrdersBeforeUpdate, systemStores);
    }

    private void updateSystemOrdersAccordingToHistoryFile (Map<UUID, List<SystemOrder>> ordersFromHistoryFile,
                                                           Descriptor descriptor,
                                                           Map<UUID, List<SystemOrder>> systemOrdersBeforeUpdate,
                                                           Map<Integer, SystemStore> systemStores) {
        ordersFromHistoryFile.entrySet()
                             .stream()
                             .filter(entry -> !systemOrdersBeforeUpdate.containsKey(entry.getKey()))
                             .forEach(entry -> entry.getValue()
                                                    .forEach(order -> updateSystemAfterLoadingHistoryOrder(descriptor,
                                                                                                           systemStores,
                                                                                                           order)));
    }

    private void updateSystemAfterLoadingHistoryOrder (Descriptor descriptor,
                                                       Map<Integer, SystemStore> systemStores,
                                                       SystemOrder systemOrder) {
        SystemCustomer systemCustomer = getCustomerById(descriptor, systemOrder.getCustomerId());
        SystemStore systemStore = getStoreById(descriptor, systemOrder.getStoreId());
        Order order = getOrder(systemStore, systemOrder);

        updateSystemAfterStaticOrderV2(systemStore, order, descriptor, systemCustomer);
    }

    private Order getOrder (SystemStore systemStore, SystemOrder systemOrder) {
        Order order = systemOrder.getOrder();
        SYSTEM_UPDATER_VALIDATOR.validateAllOrderItemsExistInStore(systemStore, order);

        return order;
    }

    private SystemCustomer getCustomerById (Descriptor descriptor, Integer customerId) {
        SYSTEM_UPDATER_VALIDATOR.validateCustomerExistInSystem(descriptor, customerId);

        return descriptor.getSystemCustomers().get(customerId);
    }

    private SystemStore getStoreById (Descriptor descriptor, Integer storeId) {
        Map<Integer, SystemStore> systemStores = descriptor.getSystemStores();

        SYSTEM_UPDATER_VALIDATOR.validateStoreExistInSystem(storeId, systemStores);

        return systemStores.get(storeId);
    }

    private void addNewOrderToSystemOrders (SystemStore systemStore, Order newOrder, Descriptor descriptor, Integer customerId) {
        List<SystemOrder> orders;
        SystemOrder newSystemOrder = new SystemOrder(newOrder, systemStore.getName(), systemStore.getId(), customerId);
        UUID id = (newOrder.getParentId() != null) ? newOrder.getParentId() : newOrder.getId();
        Map<UUID, List<SystemOrder>> systemOrders = descriptor.getSystemOrders();

        if (systemOrders.containsKey(id)) {
            orders = new ArrayList<>(systemOrders.get(id));
            orders.add(newSystemOrder);
        }
        else {
            orders = Collections.singletonList(newSystemOrder);
        }

        systemOrders.put(id, orders);
    }

    private void updateSystemStore (SystemStore systemStore, Order newOrder) {
        updateDeliveriesPayment(systemStore, newOrder);
    }

    private void updateDeliveriesPayment (SystemStore systemStore, Order newOrder) {
        systemStore.setTotalDeliveriesPayment(GeneratedDataMapper.round(systemStore.getTotalDeliveriesPayment()
                    + newOrder.getDeliveryPrice(), 2));
    }

    public void updateSystemAfterStaticOrderV2 (SystemStore systemStore,
                                                Order newOrder,
                                                Descriptor descriptor,
                                                SystemCustomer systemCustomer) {
        updateSystemStore(systemStore, newOrder);
        // add order to store
        systemStore.getOrders().add(newOrder);
        // add order to order collection in descriptor
        addNewOrderToSystemOrders(systemStore, newOrder, descriptor, systemCustomer.getId());
        // update counter of all store items that was included in order
        updateStoreAfterOrderCompletionV2(systemStore, newOrder);
        // update counter of all system items that was included in order
        updateSystemItemsAfterOrderCompletionV2(descriptor.getSystemItems(), newOrder);
        // update total delivery price and total items price for system customer (update numOfOrder in case
        // the order is not part of dynamic order)
        updateSystemCustomerAfterOrderCompletion(newOrder, systemCustomer);
    }

    private void updateSystemCustomerAfterOrderCompletion (Order newOrder, SystemCustomer systemCustomer) {
        double prevTotalDeliveryPrice = systemCustomer.getTotalDeliveryPrice();
        double prevTotalItemsPrice = systemCustomer.getTotalItemsPrice();

        systemCustomer.setTotalItemsPrice(prevTotalDeliveryPrice + newOrder.getDeliveryPrice());
        systemCustomer.setTotalDeliveryPrice(prevTotalItemsPrice + newOrder.getItemsPrice());

        if (newOrder.getParentId() == null) {
            int prevNumOfOrders = systemCustomer.getNumOfOrders();
            systemCustomer.setNumOfOrders(prevNumOfOrders + 1);
        }
    }

    private void updateStoreAfterOrderCompletionV2 (SystemStore systemStore, Order newOrder) {
        Map<Integer, StoreItem> storeItems = systemStore.getItemIdToStoreItem();
        Map<PricedItem, Double> orderItems = newOrder.getPricedItems();
        Map<Offer, Integer> orderOffers = newOrder.getSelectedOfferToNumOfRealization();

        // for order items
        for (PricedItem pricedItem : orderItems.keySet()) {
            int itemId = pricedItem.getId();

            if (storeItems.containsKey(itemId)) {
                StoreItem storeItem = storeItems.get(itemId);
                double prevNumOfPurchases = storeItem.getPurchasesCount();
                storeItem.setPurchasesCount(prevNumOfPurchases + orderItems.get(pricedItem));
            }
        }

        // for discount items
        for (Map.Entry<Offer, Integer> entry : orderOffers.entrySet()) {
            Offer currOffer = entry.getKey();
            Integer numOfRealizations = entry.getValue();
            int offerItemId = currOffer.getItemId();

            if (storeItems.containsKey(offerItemId)) {
                StoreItem storeItem = storeItems.get(offerItemId);
                double prevDiscountNumOfPurchases = storeItem.getDiscountPurchasesCount();
                storeItem.setDiscountPurchasesCount(prevDiscountNumOfPurchases + numOfRealizations * currOffer.getQuantity());
            }
        }

    }

    private void updateSystemItemsAfterOrderCompletionV2 (Map<Integer, SystemItem> allSystemItems, Order newOrder) {
        // for order items
        Map<PricedItem, Double> orderItemsToAmount = newOrder.getPricedItems();
        for (Map.Entry<PricedItem, Double> entry : orderItemsToAmount.entrySet()) {
            PricedItem pricedItem = entry.getKey();
            Double amount = entry.getValue();
            int itemId = pricedItem.getId();
            SystemItem systemItem = allSystemItems.get(itemId);
            Double prevOrdersCount = systemItem.getOrdersCount();

            systemItem.setOrdersCount(prevOrdersCount + amount);
        }

        // for discount items
        Map<Offer, Integer> orderOffersToNumOfRealizations = newOrder.getSelectedOfferToNumOfRealization();
        for (Map.Entry<Offer, Integer> entry : orderOffersToNumOfRealizations.entrySet()) {
            Offer currOffer = entry.getKey();
            Integer numOfRealizations = entry.getValue();
            SystemItem systemItem = allSystemItems.get(currOffer.getItemId());
            double prevDiscountOrdersCount = systemItem.getDiscountOrdersCount();

            systemItem.setDiscountOrdersCount(prevDiscountOrdersCount + currOffer.getQuantity() * numOfRealizations);
        }
    }
}