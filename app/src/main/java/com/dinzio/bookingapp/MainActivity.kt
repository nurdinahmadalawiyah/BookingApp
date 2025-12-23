package com.dinzio.bookingapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.dinzio.bookingapp.common.navigation.NavGraph
import com.dinzio.bookingapp.common.theme.BookingAppTheme
import com.dinzio.bookingapp.features.main.screen.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }

        setContent {
            val startDestination by viewModel.startDestination.collectAsState()
            val isLoading by viewModel.isLoading.collectAsState()

            BookingAppTheme {
                if (!isLoading && startDestination != null) {
                    val navController = rememberNavController()
                    NavGraph(
                        navController = navController,
                        startDestination = startDestination!!
                    )
                }
            }
        }
    }
}