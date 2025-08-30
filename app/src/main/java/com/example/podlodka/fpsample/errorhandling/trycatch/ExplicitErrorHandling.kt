package com.example.podlodka.fpsample.errorhandling.trycatch

fun explicitErrorHandlingExample() {
  val inputs = listOf("user_id:123", "user_id:abc", "user_id:-5", "неверный_формат")
  for (input in inputs) {
    getUserIdFromString(input)
      .onSuccess { println(it) }
      .onFailure {
        when(it) {
          is ParseError.InvalidFormat -> println(it.reason)
          is ParseError.NotANumber -> println(it.text)
          is ParseError.NegativeId -> println(it.text)
          else -> println(it.message)
        }
      }
    println("⏹️ Завершение операции для '$input'.\n")
  }
}

sealed class ParseError : Exception() {
  data class InvalidFormat(val reason: String) : ParseError()
  data class NotANumber(val text: String) : ParseError()
  data class NegativeId(val text: String) : ParseError()
}

private fun getUserIdFromString(input: String): Result<Int> {
  println("▶️ Начинаю обработку '$input'")
  return run {
    validateFormat(input)
      .flatMap { idStr -> parseToInt(idStr) }
      .flatMap { userId -> validateNotNegative(userId) }
  }
}

private fun validateFormat(input: String): Result<String> {
  val parts = input.split(':')
  return if (parts.size != 2 || parts[0] != "user_id") {
    Result.failure(
      exception = ParseError.InvalidFormat(
        reason = "Неверный формат. Ожидалось 'user_id:ЧИСЛО'."
      )
    )
  } else {
    Result.success(value = parts[1])
  }
}

private fun parseToInt(idStr: String): Result<Int> {
  return idStr.toIntOrNull()?.let { userId ->
    Result.success(value = userId)
  } ?: Result.failure(
    exception = ParseError.NotANumber(text = "Значение '$idStr' не является числом.")
  )
}

private fun validateNotNegative(userId: Int): Result<Int> {
  return if (userId < 0) {
    Result.failure(
      exception = ParseError.NegativeId(
        text = "ID пользователя не может быть отрицательным, получено: $userId."
      )
    )
  } else {
    Result.success(userId)
  }
}

private inline fun <T, R> Result<T>.flatMap(transform: (T) -> Result<R>): Result<R> {
  return when {
    isSuccess -> transform(getOrThrow())
    else -> Result.failure(exceptionOrNull()!!)
  }
}