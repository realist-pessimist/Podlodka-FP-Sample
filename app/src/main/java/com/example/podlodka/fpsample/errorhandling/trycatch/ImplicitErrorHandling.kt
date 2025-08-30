package com.example.podlodka.fpsample.errorhandling.trycatch

fun implicitErrorHandlingExample() {
  val inputs = listOf("user_id:123", "user_id:abc", "user_id:-5", "неверный_формат")
  for (input in inputs) {
    try {
      val id = getUserIdFromString(input)
      println("✔️ Найден ID: $id\n")
    } catch (e: Exception) {
      // Внешний обработчик ловит всё, но теряет специфику
      println("❌ Внешний обработчик поймал ошибку: ${e.message}\n")
    }
  }
}

private class InvalidFormatException(message: String) : IllegalArgumentException(message)
private class NegativeIdException(message: String) : IllegalArgumentException(message)

private fun getUserIdFromString(input: String): Int {
  println("▶️ Начинаю обработку '$input'")
  try {
    val parts = input.split(':')
    if (parts.size != 2 || parts[0] != "user_id") {
      throw InvalidFormatException(
        message = "Неверный формат. Ожидалось 'user_id:ЧИСЛО'."
      )
    }
    val idStr = parts[1]
    val userId = idStr.toInt()

    if (userId < 0) {
      throw NegativeIdException(
        message = "ID пользователя не может быть отрицательным, получено: $userId."
      )
    }
    println("✅ Парсинг успешен!")
    return userId
  } catch (e: NumberFormatException) {
    println("🔥 Ошибка: значение не является числом.")
    throw IllegalStateException(
      "Внутренняя ошибка парсинга", e
    )
  } catch (e: InvalidFormatException) {
    println("🔥 ${e.message}")
    throw e
  } catch (e: NegativeIdException) {
    println("🔥 ${e.message}")
    throw e
  } finally {
    println("⏹️ Завершение операции для '$input'.\n")
  }
}