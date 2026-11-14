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

    private val reviewFocusId = mutableStateOf<Long?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            YourMoneyTheme {
                AppRoot(reviewFocusId = reviewFocusId)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val id = intent?.getLongExtra(EXTRA_PENDING_EXPENSE_ID, -1L) ?: -1L
        if (id != -1L) {
            reviewFocusId.value = id
        }
    }

    companion object {
        const val EXTRA_PENDING_EXPENSE_ID = "pending_expense_id"
    }
}
