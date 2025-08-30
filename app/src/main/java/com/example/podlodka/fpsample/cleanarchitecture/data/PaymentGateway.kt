package com.example.podlodka.fpsample.cleanarchitecture.data

import arrow.core.Either
import arrow.core.raise.either
import arrow.core.raise.ensure
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingError
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.PriceDetails
import kotlinx.coroutines.delay

class PaymentGateway {
  suspend fun chargeCard(
    bookingId: String,
    price: PriceDetails
  ): Either<BookingError, Unit> = either {
    delay(100)
    ensure(price.total > 0) { BookingError.PaymentFailed(bookingId) }
  }
}