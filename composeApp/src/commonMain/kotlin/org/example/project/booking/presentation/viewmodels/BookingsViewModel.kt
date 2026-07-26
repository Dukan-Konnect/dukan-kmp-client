package org.example.project.booking.presentation.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.example.project.booking.domain.repository.BookingRemoteRepository
import org.example.project.booking.domain.repository.BookingRepository
import org.example.project.core.datastore.UserPreferencesRepository
import org.example.project.core.utils.DataState
import org.example.project.core.model.booking.Booking
import org.example.project.core.utils.SnackbarMessage
import org.example.project.core.utils.handleLogout

private fun String?.orGenericError(): String =
    this?.takeIf { message -> message.isNotBlank() && message != "null" }
        ?: SnackbarMessage.GENERIC_ERROR

@Immutable
data class BookingsUiState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed interface BookingsIntent {
    data object LoadAll : BookingsIntent
    data object Refresh : BookingsIntent
    data class CancelBooking(val id: String, val reason: String, val comment: String) : BookingsIntent
    data class RescheduleBooking(val id: String, val newDateIso: String) : BookingsIntent
    data object Logout : BookingsIntent
}

sealed interface BookingsEffect {
    data object NavigateBack : BookingsEffect
    data class NavigateToBookings(val successMessage: String? = null) : BookingsEffect
    data class ShowToast(val message: String) : BookingsEffect
}

class BookingsViewModel(
    private val bookingRepository: BookingRepository,
    private val bookingRemoteRepository: BookingRemoteRepository,
    private val prefRepository: UserPreferencesRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BookingsUiState())
    val state: StateFlow<BookingsUiState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<BookingsEffect>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect: SharedFlow<BookingsEffect> = _effect.asSharedFlow()

    init {
        handleIntent(BookingsIntent.LoadAll)
    }

    fun handleIntent(intent: BookingsIntent) {
        when (intent) {
            BookingsIntent.LoadAll -> loadBookings()
            BookingsIntent.Refresh -> refreshFromBackend()
            is BookingsIntent.CancelBooking -> executeCancelBooking(intent.id, intent.reason, intent.comment)
            is BookingsIntent.RescheduleBooking -> executeRescheduleBooking(intent.id, intent.newDateIso)
            BookingsIntent.Logout -> {
                viewModelScope.launch {
                    handleLogout(prefRepository)
                }
            }
        }
    }

    private fun loadBookings() {
        viewModelScope.launch {
            bookingRepository.observeAllBookings()
                .catch { error ->
                    _state.update {
                        it.copy(
                            errorMessage = error.message.orGenericError()
                        )
                    }
                }
                .collect { bookings ->
                    _state.update { it.copy(bookings = bookings, errorMessage = null) }
                }
        }
    }

    private var hasFetched = false

    private fun refreshFromBackend() {
        if (hasFetched) return
        hasFetched = true
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val remoteState = bookingRemoteRepository.getMyBookings()) {
                is DataState.Success -> {
                    try {
                        coroutineScope {
                            remoteState.data.map { booking ->
                                async { bookingRepository.createBooking(booking) }
                            }.awaitAll()
                        }
                    } catch (_: Exception) { }
                    _state.update { it.copy(isLoading = false, errorMessage = null) }
                }
                is DataState.Error -> _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = remoteState.message.orGenericError()
                    )
                }
                DataState.Loading -> Unit
            }
        }
    }

    private fun executeCancelBooking(id: String, reason: String, comment: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val response = bookingRemoteRepository.cancelBooking(id)) {
                is DataState.Success -> {
                    bookingRepository.createBooking(response.data)
                    _state.update { it.copy(isLoading = false) }
                    _effect.emit(BookingsEffect.NavigateToBookings("Booking cancelled successfully"))
                }
                is DataState.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.emit(BookingsEffect.ShowToast(SnackbarMessage.GENERIC_ERROR))
                }
                DataState.Loading -> Unit
            }
        }
    }

    private fun executeRescheduleBooking(id: String, newDateIso: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val response = bookingRemoteRepository.rescheduleBooking(id, newDateIso)) {
                is DataState.Success -> {
                    bookingRepository.createBooking(response.data)
                    _state.update { it.copy(isLoading = false) }
                    _effect.emit(BookingsEffect.NavigateToBookings("Booking rescheduled successfully"))
                }
                is DataState.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _effect.emit(BookingsEffect.ShowToast(SnackbarMessage.GENERIC_ERROR))
                }
                DataState.Loading -> Unit
            }
        }
    }
}
