package com.example.podlodka.fpsample.cleanarchitecture.presentation.fsm

import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingData

sealed interface BookingEvent {
  data class SetBookingData(val data: BookingData) : BookingEvent
  data object StartBooking : BookingEvent
  data object Retry : BookingEvent
  data object Reset : BookingEvent
}