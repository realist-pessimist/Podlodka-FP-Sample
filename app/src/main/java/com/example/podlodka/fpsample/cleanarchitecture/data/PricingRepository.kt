package com.example.podlodka.fpsample.cleanarchitecture.data

import arrow.core.Either
import arrow.core.raise.either
import com.example.podlodka.fpsample.cleanarchitecture.data.utils.withTimeoutOrError
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingData
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingError
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.PriceDetails

class PricingRepository(
  private val network: NetworkDataSource,
  private val cache: CacheDataSource,
  private val transformer: (PriceDetails) -> Either<BookingError, PriceDetails> = { Either.Right(it) }
) {
  suspend fun calculatePrice(data: BookingData): Either<BookingError, PriceDetails> = either {
    when (val cached = cache.getCachedPrice(data)) {
      is Either.Right -> cached.value
      is Either.Left -> {
        val networkPrice = withTimeoutOrError("price fetch") {
          network.fetchPrice(data)
        }.bind()
        val transformedPrice = transformer(networkPrice).bind()
        cache.cachePrice(data, transformedPrice)
        transformedPrice
      }
    }
  }
}