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
import com.wordmaster.app.viewmodel.StudyViewModel

/**
 * MainActivity: 接入 StudyScreen + 真实的 StudyViewModel(Room + Ebbinghaus 驱动)。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordMasterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val vm: StudyViewModel = viewModel()
                    StudyScreen(viewModel = vm)
                }
            }
        }
    }
}
