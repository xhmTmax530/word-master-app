package com.wordmaster.app

import android.app.Application

/**
 * Application 入口。后续 Room database / DI container / 初始化逻辑挂在这里。
 */
class WordMasterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // TODO(Phase 1): 初始化 Repository、Room database、预加载词库
    }
}