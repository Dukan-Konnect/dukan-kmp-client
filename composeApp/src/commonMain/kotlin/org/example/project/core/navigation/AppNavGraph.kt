package org.example.project.core.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.example.project.home.presentation.navigation.homeNavGraph
import org.example.project.onboarding.presentation.navigation.onboardingNavGraph

@Composable
fun AppNavGraph(
    startDestination: Any
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomBar(currentRoute)) {
                val currentRouteObj = getCurrentRouteObject(currentRoute)
                BottomBar(
                    currentRoute = currentRouteObj,
                    onNavigate = { route ->
                        if (route != currentRouteObj) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            onboardingNavGraph(navController)
            homeNavGraph(navController)
        }
    }
}

private fun shouldShowBottomBar(currentRoute: String?): Boolean {
    return when {
        currentRoute?.contains("HomeRoute") == true -> true
        currentRoute?.contains("BookingsRoute") == true -> true
        currentRoute?.contains("ProfileRoute") == true -> true
        else -> false
    }
}

private fun getCurrentRouteObject(currentRoute: String?): Any? {
    return when {
        currentRoute?.contains("HomeRoute") == true -> HomeRoute
        currentRoute?.contains("BookingsRoute") == true -> BookingsRoute()
        currentRoute?.contains("ProfileRoute") == true -> ProfileRoute
        else -> null
    }
}
