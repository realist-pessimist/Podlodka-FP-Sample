package com.example.podlodka.fpsample

import com.example.podlodka.fpsample.testing.FulfillmentResult
import com.example.podlodka.fpsample.testing.OrderPure
import com.example.podlodka.fpsample.testing.OrderItem
import com.example.podlodka.fpsample.testing.Warehouse
import com.example.podlodka.fpsample.testing.WarehouseStatus
import com.example.podlodka.fpsample.testing.findOptimalFulfillmentPlan
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class FulfillmentPlannerTest : DescribeSpec({

  // --- Тестовые данные ---
  val order = OrderPure(
    "ORD-101",
    listOf(OrderItem("p1", 10), OrderItem("p2", 5)), "EU"
  )

  val warehouses = listOf(
    // Идеальный кандидат, самый дешевый
    Warehouse(
      "W-DE", WarehouseStatus.ONLINE,
      mapOf("p1" to 100, "p2" to 50), mapOf("EU" to 20.0, "US" to 100.0)
    ),
    // Дорогой, но тоже подходит
    Warehouse("W-FR", WarehouseStatus.ONLINE,
      mapOf("p1" to 20, "p2" to 20), mapOf("EU" to 50.0)),
    // Не хватает товара p2
    Warehouse("W-IT", WarehouseStatus.ONLINE,
      mapOf("p1" to 100, "p2" to 3), mapOf("EU" to 15.0)),
    // Не доставляет в EU
    Warehouse("W-US", WarehouseStatus.ONLINE,
      mapOf("p1" to 100, "p2" to 100), mapOf("US" to 30.0)),
    // В оффлайне, хотя всё есть
    Warehouse("W-PL", WarehouseStatus.OFFLINE,
      mapOf("p1" to 100, "p2" to 100), mapOf("EU" to 10.0))
  )

  describe("findOptimalFulfillmentPlan") {

    it("должен выбрать самый дешевый склад из подходящих") {
      val result = findOptimalFulfillmentPlan(order, warehouses)

      result.shouldBeInstanceOf<FulfillmentResult.Success>()
      result.plan.sourceWarehouseId shouldBe "W-DE" // Не W-FR, потому что 20.0 < 50.0
      result.plan.totalShippingCost shouldBe 20.0
    }

    it("должен вернуть Failure, если ни на одном складе не хватает товаров") {
      val smallOrder = OrderPure("ORD-102", listOf(OrderItem("p1", 200)), "EU")
      val result = findOptimalFulfillmentPlan(smallOrder, warehouses)

      result.shouldBeInstanceOf<FulfillmentResult.Failure>()
      result.reason shouldBe "Ни один склад не может полностью выполнить заказ."
    }

    it("должен вернуть Failure, если все подходящие склады в оффлайне") {
      val offlineOrder = OrderPure("ORD-103", listOf(OrderItem("p1", 1)), "EU")
      val offlineWarehouses = listOf(
        Warehouse("W-PL", WarehouseStatus.OFFLINE,
          mapOf("p1" to 100), mapOf("EU" to 10.0))
      )
      val result = findOptimalFulfillmentPlan(offlineOrder, offlineWarehouses)

      result.shouldBeInstanceOf<FulfillmentResult.Failure>()
    }

    it("должен вернуть Failure, если ни один склад не доставляет в регион") {
      val usOrder = OrderPure("ORD-104", listOf(OrderItem("p1", 1)), "US")
      val euWarehouses = listOf(
        Warehouse("W-DE", WarehouseStatus.ONLINE,
          mapOf("p1" to 100), mapOf("EU" to 20.0))
      )
      val result = findOptimalFulfillmentPlan(usOrder, euWarehouses)

      result.shouldBeInstanceOf<FulfillmentResult.Failure>()
    }
  }
})