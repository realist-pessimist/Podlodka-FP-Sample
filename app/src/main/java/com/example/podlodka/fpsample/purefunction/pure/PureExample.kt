package com.example.podlodka.fpsample.purefunction.pure

import androidx.annotation.VisibleForTesting
import com.example.podlodka.fpsample.purefunction.GlobalLogger
import com.example.podlodka.fpsample.purefunction.UserRequest

fun pureExample(request: UserRequest) {
  val result = processRequestPure(request)

  result.logs.forEach { message -> GlobalLogger.log(message) }

  if (result.success) {
    println("Отправляем email пользователю...")
  } else {
    println("Показываем ошибку в UI...")
  }
}

@VisibleForTesting
fun processRequestPure(request: UserRequest): ProcessResult {
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

data class ProcessResult(val success: Boolean, val logs: List<String>)

@VisibleForTesting
fun validateEmail(email: String): ProcessResult {
  return if (email.contains("@")) {
    ProcessResult(success = true, logs = listOf("Проверка email: OK"))
  } else {
    ProcessResult(success = false, logs = listOf("Проверка email: ОШИБКА: отсутствует '@'"))
  }
}

@VisibleForTesting
fun validatePassword(pass: String): ProcessResult {
  return if (pass.length >= 8) {
    ProcessResult(success = true, logs = listOf("Проверка пароля: OK"))
  } else {
    ProcessResult(success = false, logs = listOf("Проверка пароля: ОШИБКА: длина меньше 8 символов"))
  }
}
