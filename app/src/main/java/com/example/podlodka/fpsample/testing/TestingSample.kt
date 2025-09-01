package com.example.podlodka.fpsample.testing

// --- Модели ---

// Товар в заказе
data class OrderItem(val productId: String, val quantity: Int)

// Входящий заказ
data class Order(
  val orderId: String,
  val items: List<OrderItem>,
  val shippingRegion: String
)

enum class WarehouseStatus { ONLINE, OFFLINE }

// Склад с его инвентарём и стоимостью доставки по регионам
data class Warehouse(
  val warehouseId: String,
  val status: WarehouseStatus,
  val inventory: Map<String, Int>, // productId -> quantity
  val shippingCosts: Map<String, Double> // region -> cost
)

// Успешный результат - готовый план выполнения
data class FulfillmentPlan(
  val orderId: String,
  val sourceWarehouseId: String,
  val totalShippingCost: Double
)

// Итоговый результат работы функции
sealed class FulfillmentResult {
  data class Success(val plan: FulfillmentPlan) : FulfillmentResult()
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
  order: Order,
  warehouses: List<Warehouse>
): FulfillmentResult {

  // Вся логика - это одна цепочка вызовов.
  val bestWarehouse = warehouses
    // 1. Отбрасываем неактивные склады
    .filter { it.status == WarehouseStatus.ONLINE }

    // 2. Отбрасываем те, что не доставляют в нужный регион
    .filter { it.shippingCosts.containsKey(order.shippingRegion) }

    // 3. Отбрасываем те, где не хватает товара.
    // `all` - идеальная функция для проверки "все ли элементы коллекции удовлетворяют условию".
    .filter { warehouse ->
      order.items.all { item ->
        warehouse.inventory.getOrDefault(item.productId, 0) >= item.quantity
      }
    }

    // 4. Из оставшихся кандидатов находим тот, где доставка минимальна.
    // `minByOrNull` возвращает элемент с минимальным значением или null, если коллекция пуста.
    .minByOrNull { it.shippingCosts.getValue(order.shippingRegion) }

  // 5. Преобразуем найденный склад (или его отсутствие) в итоговый результат.
  // `let` позволяет элегантно обработать non-null значение без if-проверки.
  return bestWarehouse?.let { warehouse ->
    val cost = warehouse.shippingCosts.getValue(order.shippingRegion)
    val plan = FulfillmentPlan(order.orderId, warehouse.warehouseId, cost)
    FulfillmentResult.Success(plan)
  } ?: FulfillmentResult.Failure("Ни один склад не может полностью выполнить заказ.")
}