package com.mdtuhinhasnat.smartwheelchairdatacollector

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.mdtuhinhasnat.smartwheelchairdatacollector.data.SensorDataRepository
import com.mdtuhinhasnat.smartwheelchairdatacollector.ui.MainScreen
import com.mdtuhinhasnat.smartwheelchairdatacollector.ui.MainViewModel
import com.mdtuhinhasnat.smartwheelchairdatacollector.ui.MainViewModelFactory
import com.mdtuhinhasnat.smartwheelchairdatacollector.ui.theme.SmartWheelchairTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val repository = SensorDataRepository(applicationContext)
        val viewModelFactory = MainViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[MainViewModel::class.java]

        setContent {
            SmartWheelchairTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
