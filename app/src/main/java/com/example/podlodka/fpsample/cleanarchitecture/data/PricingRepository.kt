package com.example.podlodka.fpsample.cleanarchitecture.data

import arrow.core.Either
import arrow.core.handleErrorWith
import arrow.core.raise.either
import arrow.fx.coroutines.parMap
import arrow.fx.coroutines.parZip
import com.example.podlodka.fpsample.cleanarchitecture.data.utils.withTimeoutOrError
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingData
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingError
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.PriceDetails

private const val PERCENT_DIVISOR = 100.0

class PricingRepository(
  private val network: NetworkDataSource,
  private val cache: CacheDataSource,
  private val demandService: DemandService,
  private val transformer: (PriceDetails) -> Either<BookingError, PriceDetails> = {
    Either.Right(it)
  }
) {
  suspend fun calculatePrice(data: BookingData): Either<BookingError, PriceDetails> = either {
    cache.getCachedPrice(data)
      .handleErrorWith {
        either {
          parZip(
            fa = { withTimeoutOrError("price fetch") { network.fetchPrice(data) }.bind() },
            fb = { demandService.getDemandCoefficient(data.roomType).bind() },
            fc = {
              data.services
                .parMap { service -> network.fetchServicePrice(service).bind() }
            }
          ) { networkPrice, demandCoefficient, servicePrices ->
            networkPrice
              .withAppliedDemand(servicePrices, demandCoefficient)
              .withRecalculatedTotal()
              .let(transformer)
              .bind()
          }
        }
      }.bind().also {
        cache.cachePrice(data, it)
      }
  }

  private fun PriceDetails.withAppliedDemand(
    servicePrices: List<Double>,
    demandCoefficient: Double
  ): PriceDetails {
    val newBase = (this.base + servicePrices.sum()) * demandCoefficient
    return this.copy(base = newBase)
  }

  private fun PriceDetails.withRecalculatedTotal(): PriceDetails {
    val newTotal = this.base * (1 - this.discount / PERCENT_DIVISOR)
    return this.copy(total = newTotal)
  }
}