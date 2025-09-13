package com.example.podlodka.fpsample.testing.model

import com.example.podlodka.fpsample.testing.FulfillmentPlanPure

sealed class FulfillmentResult {
  data class Success(val plan: FulfillmentPlanPure) : FulfillmentResult()
  data class Failure(val reason: String) : FulfillmentResult()
}