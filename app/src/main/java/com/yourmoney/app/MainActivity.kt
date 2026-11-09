package com.yourmoney.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.yourmoney.app.ui.navigation.AppRoot
import com.yourmoney.app.ui.theme.YourMoneyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YourMoneyTheme {
                AppRoot()
            }
        }
    }
}
