package com.example.podlodka.fpsample.memorymeasure

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.system.measureTimeMillis

data class ImmutableStats(val level: Int, val experience: Long)
data class ImmutableProfile(val bio: String, val stats: ImmutableStats)
data class ImmutableUser(val id: Int, val username: String, val profile: ImmutableProfile)

class MutableStats(var level: Int, var experience: Long)
class MutableProfile(var bio: String, var stats: MutableStats)
class MutableUser(val id: Int, val username: String, val profile: MutableProfile)

class MemoryTestViewModel : ViewModel() {

  private val dataSize = 100_000

  private val _immutableUsers = MutableStateFlow<List<ImmutableUser>>(emptyList())

  private val _mutableUsers = MutableStateFlow<List<MutableUser>>(emptyList())

  init {
    _immutableUsers.value = List(dataSize) { id ->
      ImmutableUser(
        id = id,
        username = "User $id",
        profile = ImmutableProfile(
          bio = "Bio for user $id",
          stats = ImmutableStats(level = 1, experience = 0)
        )
      )
    }
    _mutableUsers.value = List(dataSize) { id ->
      MutableUser(
        id = id,
        username = "User $id",
        profile = MutableProfile(
          bio = "Bio for user $id",
          stats = MutableStats(level = 1, experience = 0)
        )
      )
    }
  }

  /**
   * Обновляет каждый 10-й элемент, используя глубокое вложенное копирование.
   * Это создаст ОГРОМНОЕ количество новых объектов.
   */
  fun updateWithDeepCopy() {
    viewModelScope.launch {
      val currentList = _immutableUsers.value

      val time = measureTimeMillis {
        _immutableUsers.value = currentList.mapIndexed { index, user ->
          user.copy(
            profile = user.profile.copy(
              stats = user.profile.stats.copy(
                experience = user.profile.stats.experience + 100
              )
            )
          )
        }
      }
      Log.d("Performance", "Immutable update took $time ms")
    }
  }

  /**
   * Обновляет каждый 10-й элемент, мутируя существующие объекты.
   * Новые объекты не создаются, меняются только поля.
   */
  fun updateWithMutation() {
    viewModelScope.launch {
      val currentList = _mutableUsers.value

      val time = measureTimeMillis {
        currentList.forEachIndexed { index, user ->
          user.profile.stats.experience += 100
        }
        _mutableUsers.value = currentList.toList()
      }
      Log.d("Performance", "Mutable update took $time ms")
    }
  }
}
@Composable
fun MemoryTestScreen(viewModel: MemoryTestViewModel = MemoryTestViewModel()) {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Button(onClick = { viewModel.updateWithDeepCopy() }) {
      Text("Update with deep copy()")
    }

    Spacer(modifier = Modifier.height(32.dp))

    Button(onClick = { viewModel.updateWithMutation() }) {
      Text("Update with Mutation")
    }
  }
}