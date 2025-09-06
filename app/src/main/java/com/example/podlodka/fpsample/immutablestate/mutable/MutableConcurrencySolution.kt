package com.example.podlodka.fpsample.immutablestate.mutable

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

private object InventoryConcurrencyService {
  private val stock = mapOf("Яблоко" to 10, "Банан" to 2, "Кокос" to 5)

  suspend fun validateStock(order: MutableOrder) {
    println("--- Проверка наличия ---")
    delay(100)
    order.items.removeIf { item ->
      val stockLevel = stock.getOrDefault(item.productName, 0)
      if (stockLevel < item.quantity) {
        println("⚠️ Товар ${item.productName} удален (нет в наличии).")
        true
      } else {
        false
      }
    }
  }
}

private object DiscountConcurrencyService {
  suspend fun applyDiscounts(order: MutableOrder) {
    println("--- Применение скидок ---")
    delay(50)
    var totalDiscount = 0.0
    if (order.promoCode == "SALE10") {
      totalDiscount += order.subTotal * 0.10
    }
    totalDiscount += when(order.customer) {
      Customer.VIP -> 5.0
      Customer.BASIC -> 2.0
    }
    order.discountAmount = totalDiscount
  }
}

private object ShippingConcurrencyService {
  suspend fun calculateShipping(order: MutableOrder) {
    println("--- Расчет доставки ---")
    delay(20)
    val amountAfterDiscount = order.subTotal - order.discountAmount
    order.shippingCost = if (amountAfterDiscount > 50.0) 0.0 else 7.99
  }
}

suspend fun mutableConcurrencyExample() = coroutineScope {
  val sharedOrder = MutableOrder(
    customer = Customer.BASIC, // Начнем с BASIC
    items = mutableListOf(
      MutableOrderItem("Яблоко", 20.0, 2),
      MutableOrderItem("Банан", 50.0, 1),
      MutableOrderItem("Кокос", 15.0, 1)
    )
  )
  println("\nЗапускаем ДВЕ параллельные задачи для обработки ОДНОГО И ТОГО ЖЕ заказа...")
  val job1 = launch(Dispatchers.IO) {
    println("--> [VIP] Начинаем обработку...")
    sharedOrder.customer = Customer.VIP
    InventoryConcurrencyService.validateStock(sharedOrder)
    DiscountConcurrencyService.applyDiscounts(sharedOrder)
    ShippingConcurrencyService.calculateShipping(sharedOrder)
    println("--> [VIP] Готово. Результат: $sharedOrder")
  }

  val job2 = launch(Dispatchers.Default) {
    println("--> [BASIC] Начинаем обработку...")
    sharedOrder.customer = Customer.BASIC
    InventoryConcurrencyService.validateStock(sharedOrder)
    DiscountConcurrencyService.applyDiscounts(sharedOrder)
    ShippingConcurrencyService.calculateShipping(sharedOrder)
    println("--> [BASIC] Готово. Результат: $sharedOrder")
  }
  joinAll(job1, job2)
}