package com.wordmaster.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wordmaster.app.ui.theme.Amber
import com.wordmaster.app.ui.theme.Coral
import com.wordmaster.app.ui.theme.Paper

/**
 * 学习界面的控制按钮组。
 *
 * 第一行:上一词 | 翻转 | 下一词(始终显示)
 * 第二行:记住了 ✓ | 忘了 ✗(仅在翻转后显示)
 *
 * @param onPrev 上一词
 * @param onNext 下一词
 * @param onFlip 翻转卡片
 * @param onKnown 标记为已掌握
 * @param onForgotten 标记为已遗忘
 * @param isFlipped 是否已翻转(决定第二行是否显示)
 * @param modifier 修饰符
 */
@Composable
fun ControlButtons(
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onFlip: () -> Unit,
    onKnown: () -> Unit,
    onForgotten: () -> Unit,
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // 第一行:导航 + 翻转
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPrev) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "上一词",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "上一词",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            IconButton(onClick = onFlip) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = "翻转",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "翻转",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }

            IconButton(onClick = onNext) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "下一词",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "下一词",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        // 第二行:记住了 / 忘了(仅翻转后显示)
        AnimatedVisibility(
            visible = isFlipped,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(
                    onClick = onKnown,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber,
                        contentColor = Paper,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = "记住了 ✓",
                        style = MaterialTheme.typography.labelLarge,
                        color = Paper,
                    )
                }

                Button(
                    onClick = onForgotten,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .padding(start = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Coral,
                        contentColor = Paper,
                    ),
                    shape = RoundedCornerShape(16.dp),
                ) {
                    Text(
                        text = "忘了 ✗",
                        style = MaterialTheme.typography.labelLarge,
                        color = Paper,
                    )
                }
            }
        }
    }
}
