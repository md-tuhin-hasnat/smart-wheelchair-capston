package com.mdtuhinhasnat.smartwheelchairdatacollector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mdtuhinhasnat.smartwheelchairdatacollector.data.SensorDataRepository
import com.mdtuhinhasnat.smartwheelchairdatacollector.ui.MainScreen
import com.mdtuhinhasnat.smartwheelchairdatacollector.ui.MainViewModel
import com.mdtuhinhasnat.smartwheelchairdatacollector.ui.MainViewModelFactory
import com.mdtuhinhasnat.smartwheelchairdatacollector.ui.SessionDetailScreen
import com.mdtuhinhasnat.smartwheelchairdatacollector.ui.theme.SmartWheelchairTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = SensorDataRepository(applicationContext)
        val viewModelFactory = MainViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        setContent {
            SmartWheelchairTheme {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        MainScreen(
                            viewModel = viewModel,
                            onSessionSelect = { sessionId ->
                                navController.navigate("session_detail/$sessionId")
                            }
                        )
                    }
                    composable("session_detail/{sessionId}") { backStackEntry ->
                        val sessionIdStr = backStackEntry.arguments?.getString("sessionId")
                        val sessionId = sessionIdStr?.toLongOrNull()
                        val sessionData = viewModel.allSessions.value.find { it.session.sessionId == sessionId }
                        
                        if (sessionData != null) {
                            SessionDetailScreen(
                                sessionData = sessionData,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
