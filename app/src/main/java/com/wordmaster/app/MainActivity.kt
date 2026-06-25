package com.wordmaster.app

import android.os.Build
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
 *
 * 修复 B-5: enableEdgeToEdge() 仅在 Android 15+ (SDK_INT >= 35) 下有效,
 * 低版本调用是 no-op 且可能产生误导性日志,加上版本判断显式生效/跳过。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // VANILLA_ICE_CREAM = 35 (Android 15),用字面量因为 compileSdk=34 还没引入该常量
        if (Build.VERSION.SDK_INT >= 35) {
            enableEdgeToEdge()
        }
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
