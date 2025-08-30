package com.example.podlodka.fpsample.cleanarchitecture.presentation.fsm

import androidx.annotation.VisibleForTesting
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingError
import com.example.podlodka.fpsample.cleanarchitecture.domain.usecase.BookHotelUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BookingFSM(
  private val bookHotelUseCase: BookHotelUseCase
) {
  private val _state = MutableStateFlow<BookingState>(BookingState.Idle(null))
  val state: StateFlow<BookingState> = _state.asStateFlow()

  suspend fun processEvent(event: BookingEvent) {
    val newState = when (val current = _state.value) {
      is BookingState.Idle -> handleIdle(current, event)
      is BookingState.Processing -> current
      is BookingState.Success -> handleSuccess(current, event)
      is BookingState.Error -> handleError(current, event)
    }

    if (newState != _state.value) {
      _state.value = newState
    }

    if (newState is BookingState.Processing) {
      handleBooking(newState)
    }
  }

  private suspend fun handleBooking(state: BookingState.Processing) {
    val result = bookHotelUseCase(state.data).fold(
      ifLeft = { error ->
        BookingState.Error(
          message = when (error) {
            is BookingError.NoRoomsAvailable -> "No rooms available"
            else -> "Booking failed: ${error.javaClass.simpleName}"
          },
          error = error,
          data = state.data
        )
      },
      ifRight = { bookingId ->
        BookingState.Success(bookingId, state.data)
      }
    )

    _state.value = result
  }

  @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
  fun handleIdle(
    state: BookingState.Idle,
    event: BookingEvent
  ): BookingState = when (event) {
    is BookingEvent.SetBookingData -> BookingState.Idle(event.data)
    is BookingEvent.StartBooking -> state.bookingData
      ?.let { BookingState.Processing(it) }
      ?: state
    else -> state
  }

  private fun handleSuccess(
    state: BookingState.Success,
    event: BookingEvent
  ): BookingState = when (event) {
    is BookingEvent.Reset -> BookingState.Idle(null)
    else -> state
  }

  private fun handleError(
    state: BookingState.Error,
    event: BookingEvent
  ): BookingState = when (event) {
    is BookingEvent.Retry -> BookingState.Processing(state.data)
    is BookingEvent.Reset -> BookingState.Idle(null)
    else -> state
  }
}