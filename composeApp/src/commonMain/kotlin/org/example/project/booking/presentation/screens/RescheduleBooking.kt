package org.example.project.booking.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import dukaankonnect.composeapp.generated.resources.Res
import dukaankonnect.composeapp.generated.resources.ic_arrow_back
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.example.project.booking.presentation.dialogs.RescheduleConfirmDialog
import org.example.project.booking.presentation.viewmodels.BookingsEffect
import org.example.project.booking.presentation.viewmodels.BookingsIntent
import org.example.project.booking.presentation.viewmodels.BookingsViewModel
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RescheduleBookingScreen(
    bookingId: String,
    viewModel: BookingsViewModel = koinViewModel(),
    onBackClick: () -> Unit = {},
    onNavigateToBookings: (String?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val booking = state.bookings.find { it.id == bookingId }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                BookingsEffect.NavigateBack -> onBackClick()
                is BookingsEffect.NavigateToBookings -> onNavigateToBookings(effect.successMessage)
                is BookingsEffect.ShowToast -> {
                    launch {
                        snackbarHostState.showSnackbar(message = effect.message)
                    }
                }
            }
        }
    }

    val intent: (BookingsIntent) -> Unit = viewModel::handleIntent

    if (booking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val timeSlots = remember {
        listOf(
            LocalTime(8, 0), LocalTime(10, 0), LocalTime(12, 0),
            LocalTime(14, 0), LocalTime(16, 0), LocalTime(18, 0)
        )
    }

    val currentMoment = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
    val today = currentMoment.date

    val validDates = remember {
        val nowInstant = Clock.System.now()
        val twoHoursFromNow = nowInstant.plus(2.hours)

        val allTodaySlotsInvalid = timeSlots.all { time ->
            val slotDateTime = LocalDateTime(
                year = today.year,
                month = today.month,
                day = today.day,
                hour = time.hour,
                minute = time.minute
            )
            slotDateTime.toInstant(TimeZone.currentSystemDefault()) < twoHoursFromNow
        }

        val startOffset = if (allTodaySlotsInvalid) 1 else 0
        (startOffset..startOffset + 2).map { today.plus(it, DateTimeUnit.DAY) }
    }

    val timeFormatter = remember {
        LocalTime.Format {
            amPmHour()
            char(':')
            minute()
            char(' ')
            amPmMarker("AM", "PM")
        }
    }

    var selectedDate by remember { mutableStateOf(validDates.first()) }
    var selectedTime by remember { mutableStateOf<LocalTime?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) } // Dialog State

    if (showConfirmDialog) {
        RescheduleConfirmDialog(
            onDismiss = { showConfirmDialog = false },
            onCancel = { showConfirmDialog = false },
            onReschedule = {
                showConfirmDialog = false
                val finalDateTime = LocalDateTime(
                    year = selectedDate.year,
                    month = selectedDate.month,
                    day = selectedDate.day,
                    hour = selectedTime!!.hour,
                    minute = selectedTime!!.minute
                )
                val isoString = finalDateTime.toInstant(TimeZone.currentSystemDefault()).toString()
                intent(BookingsIntent.RescheduleBooking(bookingId, isoString))
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reschedule Booking") },
                windowInsets = WindowInsets(0.dp),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = booking.subServiceImage,
                        contentDescription = booking.subServiceName,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = booking.subServiceName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "• 1 hour",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "• Provider: ${booking.providerName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Select new date and time",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Your service will take approx. 1 hour",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(validDates) { date ->
                    val isSelected = date == selectedDate
                    val dayOfWeek = date.dayOfWeek.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
                    val dayOfMonth = date.day.toString()

                    Surface(
                        modifier = Modifier
                            .size(width = 64.dp, height = 72.dp)
                            .clickable {
                                selectedDate = date
                                selectedTime = null
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFFECE7FF) else Color.Transparent,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = dayOfWeek,
                                fontSize = 14.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dayOfMonth,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val chunkedTimes = timeSlots.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                chunkedTimes.forEach { rowTimes ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowTimes.forEach { time ->
                            val slotDateTime = LocalDateTime(
                                year = selectedDate.year,
                                month = selectedDate.month,
                                day = selectedDate.day,
                                hour = time.hour,
                                minute = time.minute
                            )
                            val slotInstant = slotDateTime.toInstant(TimeZone.currentSystemDefault())

                            val isPastTime = slotInstant < Clock.System.now().plus(1.hours)
                            val isSelected = time == selectedTime

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable(enabled = !isPastTime) {
                                        selectedTime = time
                                    },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFECE7FF) else if (isPastTime) Color(0xFFF9F9F9) else Color.Transparent,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else if (isPastTime) Color.Transparent else Color(0xFFE0E0E0)
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = time.format(timeFormatter),
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isPastTime) Color.LightGray else if (isSelected) MaterialTheme.colorScheme.primary else Color.Black
                                    )
                                }
                            }
                        }

                        repeat(3 - rowTimes.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val isSlotSelected = selectedTime != null
            Button(
                onClick = {
                    if (isSlotSelected) {
                        showConfirmDialog = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isSlotSelected && !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Confirm new slot",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSlotSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}