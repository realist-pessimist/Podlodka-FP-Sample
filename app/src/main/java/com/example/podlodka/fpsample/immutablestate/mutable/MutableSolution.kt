package com.example.podlodka.fpsample.immutablestate.mutable

data class MutableOrderItem(
  val productName: String,
  val price: Double,
  var quantity: Int
)

enum class Customer {
  VIP,
  BASIC,
}

class MutableOrder(
  var customer: Customer,
  val items: MutableList<MutableOrderItem>,
  var promoCode: String? = null,
  var discountAmount: Double = 0.0,
  var shippingCost: Double = 0.0
) {
  val subTotal: Double
    get() = items.sumOf { it.price * it.quantity }

  val grandTotal: Double
    get() = subTotal - discountAmount + shippingCost

  override fun toString(): String {
    return """
            Order(
                items = ${items.joinToString { it.productName + ":x" + it.quantity }},
                subTotal = $subTotal,
                discount = $discountAmount,
                shipping = $shippingCost,
                TOTAL = $grandTotal
            )
        """.trimIndent()
  }
}

private object InventoryService {
  private val stock = mapOf("Яблоко" to 10, "Банан" to 0, "Кокос" to 5)

  fun validateStock(order: MutableOrder) {
    println("--- Проверка наличия ---")
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

private object DiscountService {
  fun applyDiscounts(order: MutableOrder) {
    println("--- Применение скидок ---")
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

private object ShippingService {
  fun calculateShipping(order: MutableOrder) {
    println("--- Расчет доставки ---")
    val amountAfterDiscount = order.subTotal - order.discountAmount
    order.shippingCost = if (amountAfterDiscount > 50.0) 0.0 else 7.99
  }
}

fun mutableExample() {
  val order = MutableOrder(
    customer = Customer.VIP,
    items = mutableListOf(
      MutableOrderItem("Яблоко", 20.0, 2), // 40
      MutableOrderItem("Банан", 50.0, 1), // 50 (нет в наличии)
      MutableOrderItem("Кокос", 15.0, 1)  // 15
    ),
    promoCode = "SALE10"
  )
  println("Начальный заказ: $order\n")

  InventoryService.validateStock(order)
  DiscountService.applyDiscounts(order)
  ShippingService.calculateShipping(order)

  println("\nФинальный обработанный заказ: $order")
}