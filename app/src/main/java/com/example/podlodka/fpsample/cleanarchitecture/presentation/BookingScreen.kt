package com.example.podlodka.fpsample.cleanarchitecture.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.podlodka.fpsample.cleanarchitecture.data.AvailabilityRepository
import com.example.podlodka.fpsample.cleanarchitecture.data.BookingRepository
import com.example.podlodka.fpsample.cleanarchitecture.data.EmailRepository
import com.example.podlodka.fpsample.cleanarchitecture.data.InMemoryCacheDataSource
import com.example.podlodka.fpsample.cleanarchitecture.data.NetworkDataSourceImpl
import com.example.podlodka.fpsample.cleanarchitecture.data.PaymentGateway
import com.example.podlodka.fpsample.cleanarchitecture.data.PricingRepository
import com.example.podlodka.fpsample.cleanarchitecture.domain.model.BookingData
import com.example.podlodka.fpsample.cleanarchitecture.domain.usecase.BookHotelUseCase
import com.example.podlodka.fpsample.cleanarchitecture.presentation.fsm.BookingEvent
import com.example.podlodka.fpsample.cleanarchitecture.presentation.fsm.BookingFSM
import com.example.podlodka.fpsample.cleanarchitecture.presentation.fsm.BookingState
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun BookingFeature() {
  val networkDataSource = remember { NetworkDataSourceImpl() }
  val cacheDataSource = remember { InMemoryCacheDataSource() }

  val availabilityRepo = remember {
    AvailabilityRepository(networkDataSource, cacheDataSource)
  }
  val pricingRepo = remember {
    PricingRepository(networkDataSource, cacheDataSource)
  }
  val bookingRepo = remember {
    BookingRepository(networkDataSource)
  }

  val emailRepo = remember {
    EmailRepository(networkDataSource)
  }

  val paymentGateway = remember {
    PaymentGateway()
  }

  val bookHotelUseCase = remember {
    BookHotelUseCase(availabilityRepo, pricingRepo, bookingRepo, paymentGateway, emailRepo)
  }

  val stateMachine = remember { BookingFSM(bookHotelUseCase) }
  val viewModel = remember { BookingViewModel(stateMachine) }

  BookingScreen(viewModel)
}

@Composable
fun BookingScreen(viewModel: BookingViewModel) {
  val state by viewModel.state.collectAsState()

  when (val currentState = state) {
    is BookingState.Idle -> BookingForm(
      initialData = currentState.bookingData,
      onSetData = { data ->
        viewModel.processEvent(BookingEvent.SetBookingData(data))
      },
      onStart = {
        viewModel.processEvent(BookingEvent.StartBooking)
      }
    )

    is BookingState.Processing -> LoadingScreen()

    is BookingState.Success -> SuccessScreen(
      bookingId = currentState.bookingId,
      onReset = { viewModel.processEvent(BookingEvent.Reset) }
    )

    is BookingState.Error -> ErrorScreen(
      message = currentState.message,
      onRetry = { viewModel.processEvent(BookingEvent.Retry) },
      onReset = { viewModel.processEvent(BookingEvent.Reset) }
    )
  }
}

@Composable
fun BookingForm(
  initialData: BookingData?,
  onSetData: (BookingData) -> Unit,
  onStart: () -> Unit
) {
  val dates by remember {
    mutableStateOf(
      value = initialData?.dates ?: (LocalDate.now()..LocalDate.now().plusDays(1))
    )
  }
  var guests by remember { mutableIntStateOf(initialData?.guests ?: 1) }
  var roomType by remember { mutableStateOf(initialData?.roomType ?: "Standard") }
  var isRoomTypeMenuExpanded by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    // Поле выбора дат
    Text("Даты бронирования", style = MaterialTheme.typography.headlineSmall)
    Row(verticalAlignment = Alignment.CenterVertically) {
      val startDateStr = remember(dates) { dates.start.format(DateTimeFormatter.ISO_DATE) }
      val endDateStr = remember(dates) { dates.endInclusive.format(DateTimeFormatter.ISO_DATE) }
      Text("$startDateStr - $endDateStr")
      Spacer(modifier = Modifier.width(8.dp))
      Button(onClick = { /* TODO: Реализовать выбор дат */ }) {
        Text("Изменить")
      }
    }

    // Поле количества гостей
    Spacer(modifier = Modifier.height(16.dp))
    Text("Количество гостей", style = MaterialTheme.typography.headlineSmall)
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(onClick = { if (guests > 1) guests-- }) {
        Icon(
          Icons.AutoMirrored.Filled.KeyboardArrowLeft,
          contentDescription = "Уменьшить"
        )
      }
      Text("$guests", modifier = Modifier.width(40.dp), textAlign = TextAlign.Center)
      IconButton(onClick = { guests++ }) {
        Icon(
          Icons.AutoMirrored.Filled.KeyboardArrowRight,
          contentDescription = "Увеличить"
        )
      }
    }

    // Поле типа номера
    Spacer(modifier = Modifier.height(16.dp))
    Text("Тип номера", style = MaterialTheme.typography.headlineSmall)
    Box {
      // Кнопка для открытия меню
      OutlinedButton(
        onClick = { isRoomTypeMenuExpanded = true },
        modifier = Modifier.fillMaxWidth(0.8f)
      ) {
        Text(roomType)
        Icon(
          if (isRoomTypeMenuExpanded) Icons.Filled.KeyboardArrowUp
          else Icons.Filled.ArrowDropDown,
          contentDescription = null
        )
      }

      // Выпадающее меню
      DropdownMenu(
        expanded = isRoomTypeMenuExpanded,
        onDismissRequest = { isRoomTypeMenuExpanded = false },
        modifier = Modifier.fillMaxWidth(0.8f)
      ) {
        listOf("Standard", "Deluxe", "Suite").forEach { type ->
          DropdownMenuItem(
            text = { Text(text = type) },
            onClick = {
              roomType = type
              isRoomTypeMenuExpanded = false
            }
          )
        }
      }
    }

    // Кнопка бронирования
    Spacer(modifier = Modifier.height(24.dp))
    Button(
      onClick = {
        onSetData(BookingData(dates, guests, roomType))
        onStart()
      },
      modifier = Modifier.fillMaxWidth(0.8f),
      enabled = guests > 0
    ) {
      Text("Забронировать")
    }
  }
}

@Composable
fun LoadingScreen() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      CircularProgressIndicator()
      Spacer(modifier = Modifier.height(16.dp))
      Text("Идет бронирование...", style = MaterialTheme.typography.headlineSmall)
    }
  }
}

@Composable
fun SuccessScreen(
  bookingId: String,
  onReset: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(24.dp)
    ) {
      Icon(
        imageVector = Icons.Default.CheckCircle,
        contentDescription = "Успех",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(64.dp)
      )
      Spacer(modifier = Modifier.height(24.dp))
      Text(
        "Бронирование успешно создано!",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        "Номер брони: $bookingId",
        style = MaterialTheme.typography.bodyLarge
      )
      Spacer(modifier = Modifier.height(32.dp))
      Button(onClick = onReset) {
        Text("Создать новое бронирование")
      }
    }
  }
}

@Composable
fun ErrorScreen(
  message: String,
  onRetry: () -> Unit,
  onReset: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier.padding(24.dp)
    ) {
      Icon(
        imageVector = Icons.Default.Info,
        contentDescription = "Ошибка",
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier.size(64.dp)
      )
      Spacer(modifier = Modifier.height(24.dp))
      Text(
        "Ошибка бронирования",
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        message,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
      )
      Spacer(modifier = Modifier.height(32.dp))
      Row {
        Button(
          onClick = onRetry,
          modifier = Modifier.weight(1f),
        ) {
          Text("Повторить")
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
          onClick = onReset,
          modifier = Modifier.weight(1f)
        ) {
          Text("Начать заново")
        }
      }
    }
  }
}