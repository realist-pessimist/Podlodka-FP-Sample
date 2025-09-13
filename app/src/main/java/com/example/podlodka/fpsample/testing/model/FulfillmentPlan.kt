package com.example.podlodka.fpsample.testing.model

data class FulfillmentPlan(
  val fulfilled: List<Order>,
  val pending: List<Order>
)