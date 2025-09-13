package com.example.podlodka.fpsample.testing.model

import java.time.Instant

data class Order(
  val items: Map<String, Int>, val created: Instant
)