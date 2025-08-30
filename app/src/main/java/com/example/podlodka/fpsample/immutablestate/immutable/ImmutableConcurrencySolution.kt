package com.example.podlodka.fpsample.immutablestate.immutable

import com.example.podlodka.fpsample.immutablestate.mutable.Customer
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

suspend fun immutableConcurrencyExample() = coroutineScope {
  val initialOrderState = OrderState(
    customer = Customer.BASIC,
    items = listOf(
      OrderItem("Яблоко", 20.0, 2),
      OrderItem("Кокос", 50.0, 1),
      OrderItem("Банан", 15.0, 1)
    ),
  )
  println("\nЗапускаем ДВЕ параллельные задачи. Каждая работает со своей КОПИЕЙ данных.")
  val job1 = launch {
    println("--> [VIP] Начинаем обработку...")
    val vipState = initialOrderState.copy(customer = Customer.VIP)
    val finalVipState = processingPipeline(vipState)
    println("--> [VIP] Готово. Результат: $finalVipState")
  }
  val job2 = launch {
    println("--> [BASIC] Начинаем обработку...")
    val basicState = initialOrderState.copy(customer = Customer.BASIC)
    val finalBasicState = processingPipeline(basicState)
    println("--> [BASIC] Готово. Результат: $finalBasicState")
  }
  joinAll(job1, job2)
}

private object OrderConcurrencyProcessor {
  private val stock = mapOf("Яблоко" to 10, "Кокос" to 0, "Банан" to 5)

  suspend fun validateStock(state: OrderState): OrderState {
    println("--- Проверка наличия ---")
    delay(100)
    val (inStockItems, outOfStockItems) = state.items.partition { item ->
      stock.getOrDefault(item.productName, 0) >= item.quantity
    }
    outOfStockItems.forEach {
      println("⚠️ Товар ${it.productName} исключен (нет в наличии).")
    }
    return state.copy(items = inStockItems)
  }

  suspend fun applyDiscounts(state: OrderState): OrderState {
    println("--- Применение скидок ---")
    delay(50)
    val promoDiscount = if (state.promoCode == "SALE10") state.subTotal * 0.10 else 0.0
    val customerDiscount = when (state.customer) {
      Customer.VIP -> 5.0
      Customer.BASIC -> 2.0
    }
    return state.copy(discountAmount = promoDiscount + customerDiscount)
  }

  suspend fun calculateShipping(state: OrderState): OrderState {
    println("--- Расчет доставки ---")
    delay(20)
    val shippingCost = (state.subTotal - state.discountAmount).let { amount ->
      if (amount > 50.0) 0.0 else 7.99
    }
    return state.copy(shippingCost = shippingCost)
  }
}

private suspend fun processingPipeline(state: OrderState): OrderState {
  val validated = OrderConcurrencyProcessor.validateStock(state)
  val discounted = OrderConcurrencyProcessor.applyDiscounts(state = validated)
  return OrderConcurrencyProcessor.calculateShipping(state = discounted)
}
