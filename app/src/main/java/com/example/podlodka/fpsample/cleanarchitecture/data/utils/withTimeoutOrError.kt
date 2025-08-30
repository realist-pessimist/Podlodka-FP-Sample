package com.example.podlodka.fpsample.cleanarchitecture.data.utils

import arrow.core.Either
import arrow.core.raise.either
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingError
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import java.time.Duration

suspend fun <T : Any> withTimeoutOrError(
  operation: String,
  timeout: Duration = Duration.ofSeconds(5),
  block: suspend () -> Either<BookingError, T>
): Either<BookingError, T> = either {
  val result = Either.catch {
    withTimeout(timeout.toMillis()) {
      block().bind()
    }
  }.mapLeft { e ->
    when (e) {
      is TimeoutCancellationException -> BookingError.TimeoutError(operation)
      else -> BookingError.SystemError(e)
    }
  }
  result.bind()
}