package com.wordmaster.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * 一张支持 3D 翻转动画的卡片。
 *
 * @param front  正面内容(默认显示)
 * @param back   背面内容(翻转后显示)
 * @param flipped 是否已翻转
 * @param onFlip  触发翻转的回调
 * @param modifier 修饰符
 */
@Composable
fun FlipCard(
    front: @Composable () -> Unit,
    back: @Composable () -> Unit,
    flipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "flipRotation",
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { onFlip() },
        contentAlignment = Alignment.Center,
    ) {
        // 背面内容: rotation > 90° 时显示,并修正镜像
        if (rotation > 90f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f },
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        back()
                    }
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxSize(),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    front()
                }
            }
        }
    }
}
