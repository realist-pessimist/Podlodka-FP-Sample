package com.example.podlodka.fpsample.cleanarchitecture.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.podlodka.fpsample.cleanarchitecture.presentation.fsm.BookingEvent
import com.example.podlodka.fpsample.cleanarchitecture.presentation.fsm.BookingFSM
import kotlinx.coroutines.launch

class BookingViewModel(
  private val stateMachine: BookingFSM
) : ViewModel() {
  val state = stateMachine.state

  fun processEvent(event: BookingEvent) {
    viewModelScope.launch {
      stateMachine.processEvent(event)
    }
  }
}