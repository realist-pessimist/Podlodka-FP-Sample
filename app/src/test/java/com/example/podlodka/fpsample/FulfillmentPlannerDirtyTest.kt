package com.example.podlodka.fpsample

import com.example.podlodka.fpsample.testing.FulfillmentPlannerDirty
import com.example.podlodka.fpsample.testing.Order
import com.example.podlodka.fpsample.testing.OrderPure
import com.example.podlodka.fpsample.testing.OrderService
import com.example.podlodka.fpsample.testing.WarehouseService
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Instant

@ExtendWith(MockKExtension::class)
class FulfillmentPlannerDirtyTest : BehaviorSpec() {

  @MockK
  private lateinit var mockWarehouseService: WarehouseService

  @MockK
  private lateinit var mockOrderService: OrderService

  @InjectMockKs(overrideValues = true)
  private lateinit var planner: FulfillmentPlannerDirty

  private val warehouseId = "w-01"

  init {
    beforeTest {
      MockKAnnotations.init(this)
      planner = FulfillmentPlannerDirty(mockWarehouseService, mockOrderService, warehouseId)
    }

    Given("a fulfillment planner and a set of orders") {
      val order1 = Order(mapOf("item-1" to 3), Instant.now())
      val order2 = Order(mapOf("item-2" to 4), Instant.now().plusSeconds(1))
      val allOrders = listOf(order1, order2)

      every { mockOrderService.getPendingOrders() } returns allOrders

      When("all items are available in the warehouse") {
        val availableItems = mapOf("item-1" to 10, "item-2" to 5)
        every { mockWarehouseService.getAvailableItems(warehouseId) } returns availableItems

        val plan = planner.createFulfillmentPlan()

        Then("all orders should be fulfilled") {
          plan.fulfilled.size shouldBe 2
          plan.pending.size shouldBe 0
          plan.fulfilled shouldBe listOf(order1, order2)
        }
      }

      When("some items are not available") {
        val availableItems = mapOf("item-1" to 5) // Not enough for order2 if it also had item-1
        every { mockWarehouseService.getAvailableItems(warehouseId) } returns availableItems

        val fittingOrder = Order(mapOf("item-1" to 4), Instant.now())
        val nonFittingOrder = Order(mapOf("item-1" to 2), Instant.now().plusSeconds(1))
        every { mockOrderService.getPendingOrders() } returns listOf(fittingOrder, nonFittingOrder)

        val plan = planner.createFulfillmentPlan()

        Then("only orders that fit should be fulfilled") {
          plan.fulfilled.size shouldBe 1
          plan.pending.size shouldBe 1
          plan.fulfilled shouldBe listOf(fittingOrder)
          plan.pending shouldBe listOf(nonFittingOrder)
        }
      }
    }
  }
}