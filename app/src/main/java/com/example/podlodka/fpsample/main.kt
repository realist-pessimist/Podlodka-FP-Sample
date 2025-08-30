package com.example.podlodka.fpsample

import com.example.podlodka.fpsample.errorhandling.trycatch.explicitErrorHandlingExample
import com.example.podlodka.fpsample.errorhandling.trycatch.implicitErrorHandlingExample
import com.example.podlodka.fpsample.immutablestate.immutable.immutableConcurrencyExample
import com.example.podlodka.fpsample.immutablestate.immutable.immutableExample
import com.example.podlodka.fpsample.immutablestate.mutable.mutableConcurrencyExample
import com.example.podlodka.fpsample.immutablestate.mutable.mutableExample

suspend fun main() {
  //region Принцип иммутабельности
  //mutableExample()
  //mutableConcurrencyExample()
  //immutableExample()
  //immutableConcurrencyExample()
  //endregion
  //region Принцип чистых функций
  //endregion
  //region Принцип явной обработки ошибок
  //implicitErrorHandlingExample()
  //explicitErrorHandlingExample()
  //endregion
}