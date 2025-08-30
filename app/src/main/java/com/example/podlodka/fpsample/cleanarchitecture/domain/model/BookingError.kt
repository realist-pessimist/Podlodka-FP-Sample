package com.example.podlodka.fpsample.cleanarchitecture.domain.model

sealed interface BookingError {
  data object NoRoomsAvailable : BookingError
  data object CacheMiss : BookingError
  data class PaymentFailed(val bookingId: String) : BookingError
  data class PricingError(val message: String) : BookingError
  data class NetworkError(val code: Int) : BookingError
  data class TimeoutError(val operation: String) : BookingError
  data class TransformationError(val cause: String) : BookingError
  data class CompositeError(val errors: List<BookingError>) : BookingError
  data class SystemError(val cause: Throwable) : BookingError
}