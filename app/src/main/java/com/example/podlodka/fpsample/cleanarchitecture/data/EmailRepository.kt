package com.example.podlodka.fpsample.cleanarchitecture.data

import arrow.core.Either
import com.example.podlodka.fpsample.cleanarchitecture.data.utils.withTimeoutOrError
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingError

class EmailRepository(
    private val network: NetworkDataSource
) {
    suspend fun sendConfirmationEmail(bookingId: String): Either<BookingError, Unit> =
        withTimeoutOrError("send email") {
            network.sendConfirmationEmail(bookingId)
        }
}