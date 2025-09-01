package com.example.podlodka.fpsample.cleanarchitecture.domain.model

import java.time.LocalDate

data class BookingData(
  val dates: ClosedRange<LocalDate>,
  val guests: Int,
  val roomType: String,
  val services: List<String> = emptyList()
)