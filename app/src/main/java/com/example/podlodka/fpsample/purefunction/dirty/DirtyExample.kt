package com.example.podlodka.fpsample.purefunction.dirty

import com.example.podlodka.fpsample.purefunction.GlobalLogger
import com.example.podlodka.fpsample.purefunction.UserRequest

fun dirtyExample(request: UserRequest): Boolean {
  GlobalLogger.log("Начало валидации для ${request.email}")

  if (!request.email.contains("@")) {
    GlobalLogger.log("ОШИБКА: Email не содержит '@'.")
    return false
  }

  if (request.pass.length < 8) {
    GlobalLogger.log("ОШИБКА: Пароль слишком короткий.")
    return false
  }

  GlobalLogger.log("Валидация для ${request.email} прошла успешно.")
  return true
}