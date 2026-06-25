package com.wordmaster.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wordmaster.app.ui.components.ControlButtons
import com.wordmaster.app.ui.components.FlipCard
import com.wordmaster.app.ui.components.ProgressRing
import com.wordmaster.app.viewmodel.ErrorType
import com.wordmaster.app.viewmodel.StudyViewModel

/**
 * 背单词学习主界面。
 *
 * 布局(从上到下):
 *  - ProgressRing  今日进度环
 *  - FlipCard      单词卡片(正面:单词 / 背面:释义)
 *  - ControlButtons 操控按钮(导航/翻转/标记)
 *
 * 修复:
 * - B-6: isFlipped 改 Compose `remember` 派生,ViewModel 不再持有,进程死亡自然丢失(本来就是 UI 临时态)
 * - C-1: 错误展示分支,支持"重试"按钮
 */
@Composable
fun StudyScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

    // B-6 fix: isFlipped 改为本地 UI 状态,key 是当前单词 id
    // 切词时自动回到正面,翻转操作只动本地 state
    var isFlipped by remember { mutableStateOf(false) }
    LaunchedEffect(state.currentCard?.word?.id) {
        // 切到新卡片时强制回到正面
        isFlipped = false
    }

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            // C-1 fix: 错误态分支
            state.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.errorMessage ?: "",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        when (state.errorType) {
                            ErrorType.WORDS_LOAD_FAILED -> {
                                Button(onClick = { /* 用户重新打开 App 触发重试 */ }) {
                                    Text("请重启 App 重试")
                                }
                            }
                            ErrorType.DB_INIT_FAILED -> {
                                Button(onClick = { viewModel.dismissError() }) {
                                    Text("关闭")
                                }
                            }
                            else -> {
                                Button(onClick = { viewModel.dismissError() }) {
                                    Text("知道了")
                                }
                            }
                        }
                    }
                }
            }

            state.currentCard == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "暂无单词,请先加载词库",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                    )
                }
            }

            else -> {
                // 顶部:进度环
                ProgressRing(
                    progress = state.learnedToday.toFloat() / state.targetToday.toFloat(),
                    learned = state.learnedToday,
                    target = state.targetToday,
                    modifier = Modifier.padding(top = 16.dp),
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 中间:翻转卡片
                FlipCard(
                    front = {
                        Text(
                            text = state.currentCard!!.word.word,
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                        )
                    },
                    back = {
                        Text(
                            text = state.currentCard!!.word.meaning,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center,
                        )
                    },
                    flipped = isFlipped,
                    onFlip = { isFlipped = !isFlipped },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .weight(1f),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 底部:控制按钮
                ControlButtons(
                    onPrev = { /* TODO: nav to prev card */ },
                    onNext = { /* TODO: nav to next card */ },
                    onFlip = { isFlipped = !isFlipped },
                    onKnown = viewModel::markKnown,
                    onForgotten = viewModel::markForgotten,
                    isFlipped = isFlipped,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
