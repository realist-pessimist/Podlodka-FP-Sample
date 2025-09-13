package com.example.podlodka.fpsample.testing

import com.example.podlodka.fpsample.testing.model.FulfillmentPlan
import com.example.podlodka.fpsample.testing.model.Order
import com.example.podlodka.fpsample.testing.service.OrderService
import com.example.podlodka.fpsample.testing.service.WarehouseService

/**
 * Находит оптимальный (самый дешевый) план выполнения заказа с одного склада.
 *
 */
class FulfillmentPlannerDirty(
    private val warehouseService: WarehouseService,
    private val orderService: OrderService,
    private val warehouseId: String
) {

    fun createFulfillmentPlan(): FulfillmentPlan {
        val availableItems = warehouseService.getAvailableItems(warehouseId)
        val allOrders: List<Order> = orderService.getPendingOrders()

        val sortedOrders = allOrders.sortedBy { it.created }

        val fulfilledOrders = mutableListOf<Order>()
        val remainingItems = availableItems.toMutableMap()

        for (order in sortedOrders) {
            var canFulfill = true
            for ((item, quantity) in order.items) {
                if (remainingItems.getOrDefault(item, 0) < quantity) {
                    canFulfill = false
                    break
                }
            }

            if (canFulfill) {
                for ((item, quantity) in order.items) {
                    remainingItems[item] = remainingItems.getValue(item) - quantity
                }
                fulfilledOrders.add(order)
            }
        }

        val pendingOrders = sortedOrders - fulfilledOrders.toSet()
        return FulfillmentPlan(fulfilledOrders, pendingOrders.toList())
    }
}