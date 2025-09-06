package com.example.podlodka.fpsample.testing

data class OrderItem(val productId: String, val quantity: Int)

data class OrderPure(
  val orderId: String,
  val items: List<OrderItem>,
  val shippingRegion: String
)

enum class WarehouseStatus { ONLINE, OFFLINE }

data class Warehouse(
  val warehouseId: String,
  val status: WarehouseStatus,
  val inventory: Map<String, Int>,
  val shippingCosts: Map<String, Double>
)

data class FulfillmentPlanPure(
  val orderId: String,
  val sourceWarehouseId: String,
  val totalShippingCost: Double
)

sealed class FulfillmentResult {
  data class Success(val plan: FulfillmentPlanPure) : FulfillmentResult()
  data class Failure(val reason: String) : FulfillmentResult()
}

/**
 * Находит оптимальный (самый дешевый) план выполнения заказа с одного склада.
 * Функция построена как декларативный конвейер с использованием функций высшего порядка.
 *
 * @param order Заказ, который нужно выполнить.
 * @param warehouses Список всех доступных складов.
 * @return `FulfillmentResult` с планом или причиной сбоя.
 */
fun findOptimalFulfillmentPlan(
  order: OrderPure,
  warehouses: List<Warehouse>
): FulfillmentResult {
  val bestWarehouse = warehouses
    .filter { it.status == WarehouseStatus.ONLINE }
    .filter { it.shippingCosts.containsKey(order.shippingRegion) }
    .filter { warehouse ->
      order.items.all { item ->
        warehouse.inventory.getOrDefault(item.productId, 0) >= item.quantity
      }
    }
    .minByOrNull { it.shippingCosts.getValue(order.shippingRegion) }

  return bestWarehouse?.let { warehouse ->
    val cost = warehouse.shippingCosts.getValue(order.shippingRegion)
    val plan = FulfillmentPlanPure(order.orderId, warehouse.warehouseId, cost)
    FulfillmentResult.Success(plan)
  } ?: FulfillmentResult.Failure("Ни один склад не может полностью выполнить заказ.")
}