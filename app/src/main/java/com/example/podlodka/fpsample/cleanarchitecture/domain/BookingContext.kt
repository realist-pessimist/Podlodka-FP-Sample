package com.example.podlodka.fpsample.cleanarchitecture.domain

import arrow.core.Either
import arrow.core.raise.either
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingError

class BookingContext(
  private val compensations: List<suspend () -> Either<BookingError, Unit>>
) {
  fun addCompensation(action: suspend () -> Either<BookingError, Unit>): BookingContext =
    BookingContext(compensations + action)

  suspend fun rollback(): Either<BookingError, Unit> = either {
    compensations.fold(Unit) { _, comp ->
      comp().bind()
    }
  }

  companion object {
    fun create(): BookingContext = BookingContext(emptyList())
  }
}