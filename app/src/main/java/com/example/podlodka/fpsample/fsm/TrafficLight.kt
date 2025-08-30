package com.example.podlodka.fpsample.fsm

import com.example.podlodka.fpsample.fsm.TrafficLightState.End
import com.example.podlodka.fpsample.fsm.TrafficLightState.Begin
import com.example.podlodka.fpsample.fsm.TrafficLightState.Red
import com.example.podlodka.fpsample.fsm.TrafficLightState.YellowToRed
import com.example.podlodka.fpsample.fsm.TrafficLightState.YellowToGreen
import com.example.podlodka.fpsample.fsm.TrafficLightState.Green
import kotlinx.coroutines.delay

sealed interface TrafficLightState {
  data object Begin : TrafficLightState
  data object End : TrafficLightState
  data class Red(val timer: Int = 0) : TrafficLightState
  data class Green(val timer: Int = 0) : TrafficLightState
  data class YellowToRed(val timer: Int = 0) : TrafficLightState
  data class YellowToGreen(val timer: Int = 0) : TrafficLightState
}

// Последовательность: Красный → ЖелтыйToGreen → Зеленый → ЖелтыйToRed → Красный
private suspend fun TrafficLightState.next(): TrafficLightState = when (this) {
  is Begin -> {
    println("🚦 Starting traffic light...")
    Red()
  }

  is Red -> {
    println("🔴 RED: Stop (${timer}/3s)")
    delay(1000)
    if (timer >= 3) YellowToGreen() else Red(timer + 1)
  }

  is YellowToGreen -> {
    println("🟡 YELLOW: Prepare to go (${timer}/2s)")
    delay(1000)
    if (timer >= 2) Green() else YellowToGreen(timer + 1)
  }

  is Green -> {
    println("🟢 GREEN: Go (${timer}/3s)")
    delay(1000)
    if (timer >= 3) YellowToRed() else Green(timer + 1)
  }

  is YellowToRed -> {
    println("🟡 YELLOW: Prepare to stop (${timer}/2s)")
    delay(1000)
    if (timer >= 2) Red() else YellowToRed(timer + 1)
  }

  is End -> {
    println("⛔ Traffic light stopped")
    this
  }
}

suspend fun fsmExample(state: TrafficLightState) {
  if (state is End) return

  val nextState = state.next()
  fsmExample(nextState)
}