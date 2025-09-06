package com.example.podlodka.fpsample.testing

import java.time.Instant

/**
 * These classes simulate external dependencies, like network services or database repositories.
 * In a real application, they would fetch data from the outside world.
 */
interface WarehouseService {
    fun getAvailableItems(warehouseId: String): Map<String, Int>
}

interface OrderService {
    fun getPendingOrders(): List<Order>
}

data class Order(
  val items: Map<String, Int>, val created: Instant
)
data class FulfillmentPlan(
  val fulfilled: List<Order>,
  val pending: List<Order>
)

/**
 * This is the "dirty" (impure) implementation.
 *
 * It depends on external services (`WarehouseService`, `OrderService`) to get its data,
 * making it non-deterministic and hard to test without mocks.
 * The core logic is hidden inside and mixed with the dependency calls.
 */
class FulfillmentPlannerDirty(
    private val warehouseService: WarehouseService,
    private val orderService: OrderService,
    private val warehouseId: String
) {

    fun createFulfillmentPlan(): FulfillmentPlan {
        val availableItems = warehouseService.getAvailableItems(warehouseId)
        val allOrders = orderService.getPendingOrders()

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