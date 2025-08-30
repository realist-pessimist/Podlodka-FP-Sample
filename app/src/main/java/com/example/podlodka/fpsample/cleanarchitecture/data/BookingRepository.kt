package com.example.podlodka.fpsample.cleanarchitecture.data

import arrow.core.Either
import com.example.podlodka.fpsample.cleanarchitecture.data.utils.withTimeoutOrError
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingData
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingError

class BookingRepository(
  private val network: NetworkDataSource
) {
  suspend fun createBooking(data: BookingData): Either<BookingError, String> =
    withTimeoutOrError("create booking") {
      network.createRemoteBooking(data)
    }

  suspend fun cancelBooking(id: String): Either<BookingError, Unit> =
    withTimeoutOrError("cancel booking") {
      network.cancelRemoteBooking(id)
    }
}