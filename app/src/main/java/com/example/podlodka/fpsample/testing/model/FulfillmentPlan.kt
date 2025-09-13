package com.example.podlodka.fpsample.testing.model

import com.example.podlodka.fpsample.testing.Order

data class FulfillmentPlan(
  val fulfilled: List<Order>,
  val pending: List<Order>
)