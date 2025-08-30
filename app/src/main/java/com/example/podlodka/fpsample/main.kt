package com.example.podlodka.fpsample

import com.example.podlodka.fpsample.errorhandling.result.explicitErrorHandlingExample
import com.example.podlodka.fpsample.errorhandling.trycatch.implicitErrorHandlingExample
import com.example.podlodka.fpsample.fsm.TrafficLightState.Begin
import com.example.podlodka.fpsample.fsm.fsmExample
import com.example.podlodka.fpsample.immutablestate.immutable.immutableConcurrencyExample
import com.example.podlodka.fpsample.immutablestate.immutable.immutableExample
import com.example.podlodka.fpsample.immutablestate.mutable.mutableConcurrencyExample
import com.example.podlodka.fpsample.immutablestate.mutable.mutableExample
import com.example.podlodka.fpsample.purefunction.dirty.dirtyExample
import com.example.podlodka.fpsample.purefunction.pure.pureExample

suspend fun main() {
  //region Принцип иммутабельности
  //mutableExample()
  //mutableConcurrencyExample()
  //immutableExample()
  //immutableConcurrencyExample()
  //endregion
  //region Принцип чистых функций
  dirtyExample()
  //pureExample()
  //endregion
  //region Принцип явной обработки ошибок
  //implicitErrorHandlingExample()
  //explicitErrorHandlingExample()
  //endregion
  //region FSM
  //fsmExample(Begin)
  //endregion
}