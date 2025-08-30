package com.example.podlodka.fpsample.cleanarchitecture.presentation.fsm

import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingData
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingError

sealed interface BookingState {
  data class Idle(val bookingData: BookingData?) : BookingState
  data class Processing(val data: BookingData) : BookingState
  data class Success(val bookingId: String, val data: BookingData) : BookingState
  data class Error(val message: String, val error: BookingError, val data: BookingData) : BookingState
}