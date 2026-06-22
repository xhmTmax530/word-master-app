package com.wordmaster.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordmaster.app.ui.screen.StudyScreen
import com.wordmaster.app.ui.theme.WordMasterTheme
import com.wordmaster.app.viewmodel.MockStudyViewModel

/**
 * MainActivity:接入 StudyScreen + MockStudyViewModel(临时兜底)。
 *
 * Phase 2 由 Logic Agent 将 MockStudyViewModel 替换为真实的 StudyViewModel。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordMasterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val vm: MockStudyViewModel = viewModel()
                    StudyScreen(viewModel = vm)
                }
            }
        }
    }
}
