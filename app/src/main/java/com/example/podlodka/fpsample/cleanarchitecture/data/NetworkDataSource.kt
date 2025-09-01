package com.example.podlodka.fpsample.cleanarchitecture.data

import arrow.core.Either
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingData
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingError
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.PriceDetails
import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import kotlin.random.nextInt

interface NetworkDataSource {
  suspend fun fetchAvailability(dates: ClosedRange<LocalDate>): Either<BookingError, Int>
  suspend fun fetchPrice(data: BookingData): Either<BookingError, PriceDetails>
  suspend fun createRemoteBooking(data: BookingData): Either<BookingError, String>
  suspend fun cancelRemoteBooking(id: String): Either<BookingError, Unit>
  suspend fun sendConfirmationEmail(bookingId: String): Either<BookingError, Unit>
  suspend fun fetchServicePrice(service: String): Either<BookingError, Double>
}

class NetworkDataSourceImpl : NetworkDataSource {
  override suspend fun fetchAvailability(dates: ClosedRange<LocalDate>): Either<BookingError, Int> {
    return Either.Right(5)
  }

  override suspend fun fetchPrice(data: BookingData): Either<BookingError, PriceDetails> {
    return Either.Right(PriceDetails(100.0, 10.0, 90.0))
  }

  override suspend fun createRemoteBooking(data: BookingData): Either<BookingError, String> {
    return Either.Right("booking-${Random.nextInt(150)}")
  }

  override suspend fun cancelRemoteBooking(id: String): Either<BookingError, Unit> {
    return Either.Right(Unit)
  }

  override suspend fun sendConfirmationEmail(bookingId: String): Either<BookingError, Unit> {
    return Either.Right(Unit)
  }

  override suspend fun fetchServicePrice(service: String): Either<BookingError, Double> {
    return when (service) {
      "breakfast" -> Either.Right(15.0)
      "spa" -> Either.Right(50.0)
      "parking" -> Either.Right(10.0)
      else -> Either.Left(BookingError.PricingError("Unknown service: $service"))
    }
  }
}

interface CacheDataSource {
  suspend fun getCachedAvailability(dates: ClosedRange<LocalDate>): Either<BookingError, Int>
  suspend fun cacheAvailability(dates: ClosedRange<LocalDate>, count: Int): Either<BookingError, Unit>
  suspend fun getCachedPrice(data: BookingData): Either<BookingError, PriceDetails>
  suspend fun cachePrice(data: BookingData, price: PriceDetails): Either<BookingError, Unit>
}

class InMemoryCacheDataSource : CacheDataSource {
  private val availabilityCache = ConcurrentHashMap<ClosedRange<LocalDate>, Int>()
  private val priceCache = ConcurrentHashMap<BookingData, PriceDetails>()

  override suspend fun getCachedAvailability(dates: ClosedRange<LocalDate>): Either<BookingError, Int> {
    return availabilityCache[dates]?.let { Either.Right(it) } ?: Either.Left(BookingError.CacheMiss)
  }

  override suspend fun cacheAvailability(dates: ClosedRange<LocalDate>, count: Int): Either<BookingError, Unit> {
    availabilityCache[dates] = count
    return Either.Right(Unit)
  }

  override suspend fun getCachedPrice(data: BookingData): Either<BookingError, PriceDetails> {
    return priceCache[data]?.let { Either.Right(it) } ?: Either.Left(BookingError.CacheMiss)
  }

  override suspend fun cachePrice(data: BookingData, price: PriceDetails): Either<BookingError, Unit> {
    priceCache[data] = price
    return Either.Right(Unit)
  }
}