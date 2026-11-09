package com.yourmoney.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.yourmoney.app.ui.navigation.AppRoot
import com.yourmoney.app.ui.theme.YourMoneyTheme

class MainActivity : ComponentActivity() {

    private val navigateToReviewSignal = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            YourMoneyTheme {
                AppRoot(navigateToReviewSignal = navigateToReviewSignal)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(EXTRA_OPEN_REVIEW, false) == true) {
            navigateToReviewSignal.value = true
        }
    }

    companion object {
        const val EXTRA_OPEN_REVIEW = "open_review"
    }
}
