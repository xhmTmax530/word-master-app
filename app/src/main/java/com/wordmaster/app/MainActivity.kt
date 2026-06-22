package com.wordmaster.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.wordmaster.app.ui.theme.WordMasterTheme

/**
 * Lead 阶段 MainActivity:仅占位,展示主题已就绪。
 * Phase 2 整合时由 UI 队员接入 StudyScreen + ViewModel。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WordMasterTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    ScaffoldPlaceholder()
                }
            }
        }
    }
}

@Composable
private fun ScaffoldPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "WordMaster 骨架已就绪,等待 UI 队员接入")
    }
}