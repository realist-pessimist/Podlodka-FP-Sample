package com.example.podlodka.fpsample.cleanarchitecture.data

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.raise.either
import com.example.podlodka.fpsample.cleanarchitecture.data.utils.withTimeoutOrError
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingError
import java.time.LocalDate

class AvailabilityRepository(
  private val network: NetworkDataSource,
  private val cache: CacheDataSource,
) {
  suspend fun checkRooms(dates: ClosedRange<LocalDate>): Either<BookingError, Int> = either {
    val cachedResult = cache.getCachedAvailability(dates)
    cachedResult.fold(
      ifLeft = { _ ->
        val networkResult = withTimeoutOrError("availably check") {
          network.fetchAvailability(dates)
        }
        networkResult.flatMap { count ->
          cache.cacheAvailability(dates, count).map { count }
        }.bind()
      },
      ifRight = { cachedCount ->
        cachedCount
      }
    )
  }
}