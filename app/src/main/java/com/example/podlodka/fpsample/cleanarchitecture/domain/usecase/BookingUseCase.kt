package com.example.podlodka.fpsample.cleanarchitecture.domain.usecase

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.handleErrorWith
import arrow.core.raise.either
import arrow.core.raise.mapError
import arrow.core.raise.ensure
import arrow.fx.coroutines.parZip
import com.example.podlodka.fpsample.cleanarchitecture.data.AvailabilityRepository
import com.example.podlodka.fpsample.cleanarchitecture.data.BookingRepository
import com.example.podlodka.fpsample.cleanarchitecture.data.EmailRepository
import com.example.podlodka.fpsample.cleanarchitecture.data.PaymentGateway
import com.example.podlodka.fpsample.cleanarchitecture.data.PricingRepository
import com.example.podlodka.fpsample.cleanarchitecture.domain.BookingContext
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingData
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingError
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.PriceDetails

class BookHotelUseCase(
  private val availabilityRepo: AvailabilityRepository,
  private val pricingRepo: PricingRepository,
  private val bookingRepo: BookingRepository,
  private val paymentGateway: PaymentGateway,
  private val emailRepo: EmailRepository,
) {
  suspend operator fun invoke(data: BookingData): Either<BookingError, String> = either {
    parZip(
      { availabilityRepo.checkRooms(data.dates).bind() },
      { pricingRepo.calculatePrice(data).bind() }
    ) { rooms, price ->
      ensure(rooms > 0) { BookingError.NoRoomsAvailable }
      performTransactionalSteps(data, price).bind()
    }
  }

  private suspend fun performTransactionalSteps(
    data: BookingData,
    price: PriceDetails
  ): Either<BookingError, String> {
    return bookingRepo.createBooking(data).flatMap { bookingId ->
      val context = BookingContext.create().addCompensation {
        bookingRepo.cancelBooking(bookingId)
      }
      paymentGateway.chargeCard(bookingId, price)
        .flatMap {
          context.addCompensation {
            paymentGateway.refundCard(bookingId)
          }
          emailRepo.sendConfirmationEmail(bookingId)
        }
        .map { bookingId }
        .handleErrorWith { error ->
          either {
            context.rollback().bind()
            raise(error)
          }
        }
    }
  }
}