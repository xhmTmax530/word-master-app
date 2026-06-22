package com.wordmaster.app.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.wordmaster.app.ui.components.ControlButtons
import com.wordmaster.app.ui.components.FlipCard
import com.wordmaster.app.ui.components.ProgressRing
import com.wordmaster.app.viewmodel.StudyViewModel

/**
 * 背单词学习主界面。
 *
 * 布局(从上到下):
 *  - ProgressRing  今日进度环
 *  - FlipCard      单词卡片(正面:单词 / 背面:释义)
 *  - ControlButtons 操控按钮(导航/翻转/标记)
 *
 * @param viewModel 提供 UI 状态;目前使用 MockStudyViewModel,Phase 2 替换为真实 ViewModel
 * @param modifier 修饰符
 */
@Composable
fun StudyScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

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
                    flipped = state.isFlipped,
                    onFlip = viewModel::flipCard,
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
                    onFlip = viewModel::flipCard,
                    onKnown = viewModel::markKnown,
                    onForgotten = viewModel::markForgotten,
                    isFlipped = state.isFlipped,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
