package com.example.podlodka.fpsample.purefunction.pure

import com.example.podlodka.fpsample.purefunction.GlobalLogger
import com.example.podlodka.fpsample.purefunction.UserRequest

fun pureExample() {
  val badRequest = UserRequest("test.com", "12345678")
  val result = processRequestPure(badRequest)

  result.logs.forEach { message -> GlobalLogger.log(message) }

  if (result.success) {
    println("Отправляем email пользователю...")
  } else {
    println("Показываем ошибку в UI...")
  }
}

private fun processRequestPure(request: UserRequest): ProcessResult {
  val initialLog = "Начало валидации для ${request.email}"

  val emailResult = validateEmail(request.email)
  if (!emailResult.success) {
    return ProcessResult(success = false, logs = listOf(initialLog) + emailResult.logs)
  }

  val passwordResult = validatePassword(request.pass)
  if (!passwordResult.success) {
    return ProcessResult(
      success = false,
      logs = listOf(initialLog) + emailResult.logs + passwordResult.logs
    )
  }

  val finalLogs = listOf(initialLog) + emailResult.logs + passwordResult.logs + "Валидация успешна."
  return ProcessResult(success = true, logs = finalLogs)
}

private data class ProcessResult(val success: Boolean, val logs: List<String>)

private fun validateEmail(email: String): ProcessResult {
  return if (email.contains("@")) {
    ProcessResult(success = true, logs = listOf("Проверка email: OK"))
  } else {
    ProcessResult(success = false, logs = listOf("Проверка email: ОШИБКА: отсутствует '@'"))
  }
}

private fun validatePassword(pass: String): ProcessResult {
  return if (pass.length >= 8) {
    ProcessResult(success = true, logs = listOf("Проверка пароля: OK"))
  } else {
    ProcessResult(success = false, logs = listOf("Проверка пароля: ОШИБКА: длина меньше 8 символов"))
  }
}
