package com.example.podlodka.fpsample.cleanarchitecture.data

import arrow.core.Either
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingError
import kotlinx.coroutines.delay

class DemandService {
  suspend fun getDemandCoefficient(roomType: String): Either<BookingError, Double> {
    delay(150)
    return when(roomType) {
      "Standard" -> Either.Right(1.0)
      "Deluxe" -> Either.Right(1.5)
      "Suite" -> Either.Right(2.0)
      else -> Either.Left(
        BookingError.PricingError("Unknown room type: $roomType")
      )
    }
  }
}