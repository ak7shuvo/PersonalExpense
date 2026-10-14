package com.ak7shuvo.personalexpense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.ak7shuvo.personalexpense.ui.AppRoot
import com.ak7shuvo.personalexpense.ui.LedgerViewModel
import com.ak7shuvo.personalexpense.ui.theme.PersonalExpenseTheme

class MainActivity : ComponentActivity() {
    private val viewModel: LedgerViewModel by viewModels { LedgerViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            PersonalExpenseTheme {
                AppRoot(viewModel)
            }
        }
    }
}
