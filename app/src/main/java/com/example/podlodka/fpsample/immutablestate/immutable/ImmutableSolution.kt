package com.example.podlodka.fpsample.immutablestate.immutable

import com.example.podlodka.fpsample.immutablestate.mutable.Customer

data class OrderItem(
  val productName: String,
  val price: Double,
  val quantity: Int
)

data class OrderState(
  val customer: Customer,
  val items: List<OrderItem>,
  val promoCode: String? = null,
  val discountAmount: Double = 0.0,
  val shippingCost: Double = 0.0
) {
  val subTotal: Double
    get() = items.sumOf { it.price * it.quantity }

  val grandTotal: Double
    get() = subTotal - discountAmount + shippingCost

  override fun toString(): String {
    return """
            OrderState(
                items = ${items.joinToString { it.productName + ": x" + it.quantity }},
                subTotal = $subTotal,
                discount = $discountAmount,
                shipping = $shippingCost,
                TOTAL = $grandTotal
            )
        """.trimIndent()
  }
}

object OrderProcessor {
  private val stock = mapOf("Яблоко" to 10, "Кокос" to 0, "Банан" to 5)

  fun validateStock(state: OrderState): OrderState {
    println("--- Проверка наличия ---")
    val (inStockItems, outOfStockItems) = state.items.partition { item ->
      stock.getOrDefault(item.productName, 0) >= item.quantity
    }
    outOfStockItems.forEach {
      println("⚠️ Товар ${it.productName} исключен (нет в наличии).")
    }
    return state.copy(items = inStockItems)
  }

  fun applyDiscounts(state: OrderState): OrderState {
    println("--- Применение скидок ---")
    val promoDiscount = if (state.promoCode == "SALE10") state.subTotal * 0.10 else 0.0
    val customerDiscount = when (state.customer) {
      Customer.VIP -> 5.0
      Customer.BASIC -> 2.0
    }
    return state.copy(discountAmount = promoDiscount + customerDiscount)
  }

  fun calculateShipping(state: OrderState): OrderState {
    println("--- Расчет доставки ---")
    val shippingCost = (state.subTotal - state.discountAmount).let { amount ->
      if (amount > 50.0) 0.0 else 7.99
    }
    return state.copy(shippingCost = shippingCost)
  }
}

fun immutableExample() {
  // 1. Создаем первоначальное, неизменяемое состояние
  val initialOrderState = OrderState(
    customer = Customer.VIP,
    items = listOf(
      OrderItem("Яблоко", 20.0, 2), // 40
      OrderItem("Кокос", 50.0, 1), // 50
      OrderItem("Банан", 15.0, 1)  // 15
    ),
    promoCode = "SALE10"
  )
  println("Начальное состояние: $initialOrderState\n")

  val validatedState = OrderProcessor.validateStock(initialOrderState)
  val discountedState = OrderProcessor.applyDiscounts(validatedState)
  val finalState = OrderProcessor.calculateShipping(discountedState)

  println("\n--- Результаты каждого шага: ---")
  println("После проверки: $validatedState")
  println("После скидок:   $discountedState")
  println("Финальное состояние: $finalState")

  println("\nПервоначальный объект НЕ ИЗМЕНИЛСЯ: $initialOrderState")
}
