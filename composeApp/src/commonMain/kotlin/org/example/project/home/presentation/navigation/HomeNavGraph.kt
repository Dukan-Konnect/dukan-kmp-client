package org.example.project.home.presentation.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import org.example.project.booking.presentation.screens.BookingDetailScreen
import org.example.project.core.navigation.BookingsRoute
import org.example.project.core.navigation.EditAddressRoute
import org.example.project.core.navigation.HomeRoute
import org.example.project.core.navigation.ManageAddressRoute
import org.example.project.core.navigation.ProfileRoute
import org.example.project.booking.presentation.screens.BookingsScreen
import org.example.project.booking.presentation.screens.CancelBookingScreen
import org.example.project.booking.presentation.screens.RescheduleBookingScreen
import org.example.project.core.navigation.BookingDetailRoute
import org.example.project.core.navigation.CancelBookingRoute
import org.example.project.core.navigation.RescheduleBookingRoute
import org.example.project.home.presentation.screens.HomeScreen
import org.example.project.home.presentation.screens.ServiceDetailScreen
import org.example.project.home.presentation.screens.SummaryScreen
import org.example.project.onboarding.presentation.navigation.LocationFetchRoute
import org.example.project.home.presentation.screens.AddAddressScreen
import org.example.project.profile.presentation.screens.EditProfileScreen
import org.example.project.profile.presentation.screens.ManageAddressScreen
import org.example.project.profile.presentation.screens.ProfileScreen


fun NavController.navigateToHomeScreen(navOptions: NavOptions? = null) = navigate(HomeRoute, navOptions)
fun NavController.navigateToBookingsScreen(
    successMessage: String? = null,
    navOptions: NavOptions? = null
) = navigate(BookingsRoute(successMessage), navOptions)
fun NavController.navigateToProfileScreen(navOptions: NavOptions? = null) = navigate(ProfileRoute, navOptions)
fun NavController.navigateToServiceDetailScreen(serviceId: Long) = navigate(ServiceDetailRoute(serviceId))
fun NavController.navigateToSummaryScreen(route: SummaryRoute) = navigate(route)
fun NavController.navigateToEditProfileScreen() = navigate(EditProfileRoute)
fun NavController.navigateToManageAddressScreen() = navigate(ManageAddressRoute)
fun NavController.navigateToEditAddressScreen(addressId: String = "") = navigate(EditAddressRoute(addressId))
fun NavController.navigateToBookingDetailScreen(bookingId: String) = navigate(BookingDetailRoute(bookingId))
fun NavController.navigateToCancelBookingScreen(bookingId: String) = navigate(CancelBookingRoute(bookingId))
fun NavController.navigateToRescheduleBookingScreen(bookingId: String) = navigate(RescheduleBookingRoute(bookingId))

fun NavGraphBuilder.homeDestination(navController: NavController) {
    composable<HomeRoute> {
        HomeScreen(
            onServiceClick = { id ->
                navController.navigateToServiceDetailScreen(id.toLong())
            },
            onLocationClick = {
                navController.navigate(LocationFetchRoute) {
                    popUpTo<HomeRoute> { inclusive = true }
                }
            }
        )
    }
}

fun NavGraphBuilder.bookingsDestination(navController: NavController) {
    composable<BookingsRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<BookingsRoute>()
        BookingsScreen(
            successMessage = route.successMessage,
            onBookingClick = { bookingId ->
                navController.navigateToBookingDetailScreen(bookingId)
            }
        )
    }
}

fun NavGraphBuilder.bookingDetailDestination(navController: NavController) {
    composable<BookingDetailRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<BookingDetailRoute>()
        BookingDetailScreen(
            bookingId = route.bookingId,
            onBackClick = navController::navigateUp,
            onRescheduleClick = { id -> navController.navigateToRescheduleBookingScreen(id) },
            onCancelClick = { id -> navController.navigateToCancelBookingScreen(id) }
        )
    }
}

fun NavGraphBuilder.cancelBookingDestination(navController: NavController) {
    composable<CancelBookingRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<CancelBookingRoute>()
        CancelBookingScreen(
            bookingId = route.bookingId,
            onBackClick = navController::navigateUp,
            onRescheduleClick = { id ->
                navController.navigateToRescheduleBookingScreen(id)
            },
            onNavigateToBookings = { successMessage ->
                navController.popBackStack<HomeRoute>(inclusive = false)
                navController.navigate(BookingsRoute(successMessage)) {
                    popUpTo<HomeRoute> { inclusive = false }
                }
            }
        )
    }
}

fun NavGraphBuilder.rescheduleBookingDestination(navController: NavController) {
    composable<RescheduleBookingRoute> { backStackEntry ->
        val route = backStackEntry.toRoute<RescheduleBookingRoute>()
        RescheduleBookingScreen(
            bookingId = route.bookingId,
            onBackClick = navController::navigateUp,
            onNavigateToBookings = { successMessage ->
                navController.popBackStack<HomeRoute>(inclusive = false)
                navController.navigate(BookingsRoute(successMessage)) {
                    popUpTo<HomeRoute> { inclusive = false }
                }
            }
        )
    }
}

fun NavGraphBuilder.profileDestination(navController: NavController) {
    composable<ProfileRoute> {
        ProfileScreen(
            onEditProfileClick = navController::navigateToEditProfileScreen,
            onNavigateToManageAddress = navController::navigateToManageAddressScreen
        )
    }
}

fun NavGraphBuilder.manageAddressDestination(navController: NavController) {
    composable<ManageAddressRoute> {
        ManageAddressScreen(
            onBack = navController::navigateUp,
            onNavigateToAddAddress = { navController.navigateToEditAddressScreen() },
            onNavigateToEditAddress = navController::navigateToEditAddressScreen
        )
    }
}

fun NavGraphBuilder.editProfileDestination(navController: NavController) {
    composable<EditProfileRoute> {
        EditProfileScreen(
            onBack = navController::navigateUp
        )
    }
}

fun NavGraphBuilder.editAddressDestination(navController: NavController) {
    composable<EditAddressRoute> {
        AddAddressScreen(
            onClose = navController::navigateUp,
            onSaveAndProceed = navController::navigateUp
        )
    }
}

fun NavGraphBuilder.serviceDetailDestination(navController: NavController) {
    composable<ServiceDetailRoute> {
        ServiceDetailScreen(
            onBackClick = navController::navigateUp,
            onNavigateToSummary = navController::navigateToSummaryScreen
        )
    }
}

fun NavGraphBuilder.summaryDestination(navController: NavController) {
    composable<SummaryRoute> {
        SummaryScreen(
            onBack = navController::navigateUp,
            onPay = { successMessage ->
                navController.popBackStack<HomeRoute>(inclusive = false)

                navController.navigate(BookingsRoute(successMessage)) {
                    popUpTo<HomeRoute> {
                        inclusive = false
                    }
                }
            },
            onNavigateToAddAddress = { navController.navigateToEditAddressScreen() }
        )
    }
}

// --- Main Graph Assembly ---
fun NavGraphBuilder.homeNavGraph(navController: NavController) {
    homeDestination(navController)
    bookingsDestination(navController)
    profileDestination(navController)
    manageAddressDestination(navController)
    editProfileDestination(navController)
    editAddressDestination(navController)
    serviceDetailDestination(navController)
    summaryDestination(navController)
    bookingDetailDestination(navController)
    cancelBookingDestination(navController)
    rescheduleBookingDestination(navController)
}
