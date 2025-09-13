package com.example.podlodka.fpsample

import com.example.podlodka.fpsample.testing.FulfillmentPlannerDirty
import com.example.podlodka.fpsample.testing.model.Order
import com.example.podlodka.fpsample.testing.service.OrderService
import com.example.podlodka.fpsample.testing.service.WarehouseService
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
internal class FulfillmentPlannerDirtyTest : BehaviorSpec() {

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

    Given(name = "a fulfillment planner and a set of orders") {
      val order1 = Order(
        items = mapOf(pair = "item-1" to 3),
        created = Instant.now()
      )
      val order2 = Order(
        items = mapOf(pair = "item-2" to 4),
        created = Instant.now().plusSeconds(1)
      )
      val allOrders = listOf(order1, order2)

      every { mockOrderService.getPendingOrders() } returns allOrders

      When(name = "all items are available in the warehouse") {
        val availableItems = mapOf("item-1" to 10, "item-2" to 5)
        every { mockWarehouseService.getAvailableItems(warehouseId) } returns availableItems

        val plan = planner.createFulfillmentPlan()

        Then(name = "all orders should be fulfilled") {
          plan.fulfilled.size shouldBe 2
          plan.pending.size shouldBe 0
          plan.fulfilled shouldBe listOf(order1, order2)
        }
      }

      When(name = "some items are not available") {
        val availableItems = mapOf("item-1" to 5)
        every { mockWarehouseService.getAvailableItems(warehouseId) } returns availableItems

        val fittingOrder = Order(
          items = mapOf(pair = "item-1" to 4),
          created = Instant.now()
        )
        val nonFittingOrder = Order(
          items = mapOf(pair = "item-1" to 2),
          created = Instant.now().plusSeconds(1)
        )
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