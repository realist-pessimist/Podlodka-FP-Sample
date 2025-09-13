package com.example.podlodka.fpsample

import com.example.podlodka.fpsample.testing.model.FulfillmentResult
import com.example.podlodka.fpsample.testing.OrderPure
import com.example.podlodka.fpsample.testing.OrderItem
import com.example.podlodka.fpsample.testing.Warehouse
import com.example.podlodka.fpsample.testing.WarehouseStatus
import com.example.podlodka.fpsample.testing.findOptimalFulfillmentPlan
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

internal class FulfillmentPlannerTest : DescribeSpec(body = {

  val order = OrderPure(
    orderId = "ORD-101",
    items = listOf(
      OrderItem(productId = "p1", quantity = 10),
      OrderItem(
        productId = "p2",
        quantity = 5)
    ),
    shippingRegion = "EU"
  )

  val warehouses = listOf(
    // Идеальный кандидат, самый дешевый
    Warehouse(
      warehouseId = "W-DE", status = WarehouseStatus.ONLINE,
      inventory = mapOf("p1" to 100, "p2" to 50), shippingCosts = mapOf("EU" to 20.0, "US" to 100.0)
    ),
    // Дорогой, но тоже подходит
    Warehouse(
      warehouseId = "W-FR", status = WarehouseStatus.ONLINE,
      inventory = mapOf("p1" to 20, "p2" to 20), shippingCosts = mapOf("EU" to 50.0)
    ),
    // Не хватает товара p2
    Warehouse(
      warehouseId = "W-IT", status = WarehouseStatus.ONLINE,
      inventory = mapOf("p1" to 100, "p2" to 3), shippingCosts = mapOf("EU" to 15.0)
    ),
    // Не доставляет в EU
    Warehouse(
      warehouseId = "W-US", status = WarehouseStatus.ONLINE,
      inventory = mapOf("p1" to 100, "p2" to 100), shippingCosts = mapOf("US" to 30.0)
    ),
    // В оффлайне, хотя всё есть
    Warehouse(
      warehouseId = "W-PL", status = WarehouseStatus.OFFLINE,
      inventory = mapOf("p1" to 100, "p2" to 100), shippingCosts = mapOf("EU" to 10.0)
    )
  )

  describe("findOptimalFulfillmentPlan") {

    it(name = "должен выбрать самый дешевый склад из подходящих") {
      val result = findOptimalFulfillmentPlan(order, warehouses)

      result.shouldBeInstanceOf<FulfillmentResult.Success>()
      result.plan.sourceWarehouseId shouldBe "W-DE"
      result.plan.totalShippingCost shouldBe 20.0
    }

    it(name = "должен вернуть Failure, если ни на одном складе не хватает товаров") {
      val smallOrder = OrderPure(
        orderId = "ORD-102",
        items = listOf(OrderItem(productId = "p1", quantity = 200)),
        shippingRegion = "EU"
      )
      val result = findOptimalFulfillmentPlan(smallOrder, warehouses)

      result.shouldBeInstanceOf<FulfillmentResult.Failure>()
      result.reason shouldBe "Ни один склад не может полностью выполнить заказ."
    }

    it("должен вернуть Failure, если все подходящие склады в оффлайне") {
      val offlineOrder = OrderPure(
        orderId = "ORD-103",
        items = listOf(OrderItem(productId = "p1", quantity = 1)),
        shippingRegion = "EU"
      )
      val offlineWarehouses = listOf(
        Warehouse(
          warehouseId = "W-PL", status = WarehouseStatus.OFFLINE,
          inventory = mapOf("p1" to 100), shippingCosts = mapOf("EU" to 10.0)
        )
      )
      val result = findOptimalFulfillmentPlan(offlineOrder, offlineWarehouses)

      result.shouldBeInstanceOf<FulfillmentResult.Failure>()
    }

    it("должен вернуть Failure, если ни один склад не доставляет в регион") {
      val usOrder = OrderPure(
        orderId = "ORD-104",
        items = listOf(OrderItem("p1", 1)),
        shippingRegion = "US"
      )
      val euWarehouses = listOf(
        Warehouse(
          warehouseId = "W-DE", status = WarehouseStatus.ONLINE,
          inventory = mapOf("p1" to 100), shippingCosts = mapOf("EU" to 20.0)
        )
      )
      val result = findOptimalFulfillmentPlan(usOrder, euWarehouses)

      result.shouldBeInstanceOf<FulfillmentResult.Failure>()
    }
  }
})