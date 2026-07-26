package org.example.project.booking.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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
import dukaankonnect.composeapp.generated.resources.ic_calendar_clock
import dukaankonnect.composeapp.generated.resources.ic_edit
import dukaankonnect.composeapp.generated.resources.ic_location
import dukaankonnect.composeapp.generated.resources.ic_star
import org.example.project.core.model.booking.Booking
import org.example.project.core.model.booking.BookingStatus
import org.example.project.booking.presentation.viewmodels.BookingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.example.project.booking.presentation.viewmodels.BookingsIntent
import org.example.project.booking.util.formatDate
import org.example.project.booking.util.formatScheduledDateString
import org.example.project.core.presentation.GenericErrorScreen
import org.jetbrains.compose.resources.painterResource

@Composable
fun BookingsScreen(
    viewModel: BookingsViewModel = koinViewModel(),
    successMessage: String? = null,
    onBookingClick: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var hasConsumedSuccessMessage by rememberSaveable(successMessage) { mutableStateOf(false) }

    val tabs = listOf("Upcoming", "Completed", "Cancelled")
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val filteredBookings = remember(state.bookings, selectedTabIndex) {
        state.bookings.filter { booking ->
            when (selectedTabIndex) {
                0 -> booking.status == BookingStatus.PENDING || booking.status == BookingStatus.CONFIRMED
                1 -> booking.status == BookingStatus.COMPLETED
                2 -> booking.status == BookingStatus.CANCELLED
                else -> false
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.handleIntent(BookingsIntent.Refresh)
    }

    LaunchedEffect(successMessage) {
        if (!hasConsumedSuccessMessage && !successMessage.isNullOrBlank()) {
            hasConsumedSuccessMessage = true
            snackbarHostState.showSnackbar(successMessage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "My Bookings",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }
            }

            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = Color(0xFF6C4DFF),
                divider = { Divider(color = Color(0xFFE0E0E0)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Color(0xFF6C4DFF) else Color.Gray
                            )
                        }
                    )
                }
            }

            when {
                state.isLoading && state.bookings.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF6C4DFF))
                    }
                }
                state.errorMessage != null && state.bookings.isEmpty() -> {
                    GenericErrorScreen(
                        title = "Failed to load bookings",
                        message = state.errorMessage!!,
                        onRetry = { viewModel.handleIntent(BookingsIntent.Refresh) },
                        onLogout = { viewModel.handleIntent(BookingsIntent.Logout) }
                    )
                }
                filteredBookings.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_edit),
                                contentDescription = "No bookings",
                                modifier = Modifier.size(80.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No ${tabs[selectedTabIndex].lowercase()} bookings",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Your bookings will appear here",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredBookings) { booking ->
                            BookingCard(
                                booking = booking,
                                onClick = { onBookingClick(booking.id) }
                            )
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
fun BookingCard(
    booking: Booking,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${booking.orderId.takeLast(8)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
                StatusChip(status = booking.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = booking.subServiceName,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = booking.providerImage,
                    contentDescription = booking.providerName,
                    modifier = Modifier
                        .width(45.dp)
                        .height(60.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        booking.providerName,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        booking.providerPhone,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_star),
                            contentDescription = "Rating",
                            tint = Color(0xFFFFA000),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            booking.providerRating.toString(),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "₹ ${booking.totalAmountCents / 100}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = Color(0xFFE0E0E0))

            Spacer(modifier = Modifier.height(12.dp))

            if (!booking.scheduledDate.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_calendar_clock),
                        contentDescription = "Time",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = formatScheduledDateString(booking.scheduledDate),
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            if (!booking.address.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_location),
                        contentDescription = "Location",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        booking.address,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Booked on ${formatDate(booking.bookingDate)}",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StatusChip(status: BookingStatus) {
    val (backgroundColor, textColor, text) = when (status) {
        BookingStatus.PENDING -> Triple(Color(0xFFFFF3E0), Color(0xFFFF6F00), "Pending")
        BookingStatus.CONFIRMED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Confirmed")
        BookingStatus.COMPLETED -> Triple(Color(0xFFE0F2F1), Color(0xFF00695C), "Completed")
        BookingStatus.CANCELLED -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "Cancelled")
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
        )
    }
}
