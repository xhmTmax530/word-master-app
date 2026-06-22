package com.wordmaster.app

import android.app.Application
import com.wordmaster.app.data.db.WordDatabase

/**
 * Application 入口。后续 Room database / DI container / 初始化逻辑挂在这里。
 */
class WordMasterApp : Application() {
    /**
     * Singleton database instance.
     */
    val database: WordDatabase
        get() = WordDatabase.getInstance(this)

    override fun onCreate() {
        super.onCreate()
        // Initialize database eagerly to avoid lazy initialization issues
        WordDatabase.getInstance(this)
        // TODO(Phase 1): 初始化 Repository、Room database、预加载词库
    }
}
