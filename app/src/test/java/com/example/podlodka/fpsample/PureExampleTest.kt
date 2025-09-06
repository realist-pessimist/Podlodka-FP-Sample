package com.example.podlodka.fpsample

import com.example.podlodka.fpsample.purefunction.UserRequest
import com.example.podlodka.fpsample.purefunction.pure.ProcessResult
import com.example.podlodka.fpsample.purefunction.pure.processRequestPure
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

internal class PureExampleTest : StringSpec({

  "processRequestPure should return success for a valid request" {
    val validInput = UserRequest(email = "user@example.com", pass = "password123")
    val expectedResult = ProcessResult(
      success = true,
      logs = listOf(
        "Начало валидации для user@example.com",
        "Проверка email: OK",
        "Проверка пароля: OK",
        "Валидация успешна."
      )
    )
    val actualResult = processRequestPure(request = validInput)
    actualResult shouldBe expectedResult
  }

  "processRequestPure should return failure for an invalid email" {
    val invalidEmailInput = UserRequest(email = "userexample.com", pass = "password123")
    val expectedResult = ProcessResult(
      success = false,
      logs = listOf(
        "Начало валидации для userexample.com",
        "Проверка email: ОШИБКА: отсутствует '@'"
      )
    )
    val actualResult = processRequestPure(request = invalidEmailInput)
    actualResult shouldBe expectedResult
  }

  "processRequestPure should return failure for an invalid password" {
    val invalidPasswordInput = UserRequest(email = "user@example.com", pass = "123")
    val expectedResult = ProcessResult(
      success = false,
      logs = listOf(
        "Начало валидации для user@example.com",
        "Проверка email: OK",
        "Проверка пароля: ОШИБКА: длина меньше 8 символов"
      )
    )
    val actualResult = processRequestPure(request = invalidPasswordInput)
    actualResult shouldBe expectedResult
  }
})