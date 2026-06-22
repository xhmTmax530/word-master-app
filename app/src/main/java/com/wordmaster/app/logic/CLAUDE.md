# logic 层

## 模块职责
纯业务逻辑(无 Android 依赖,无 Compose),可单元测试。

## 关键文件
| 文件 | 作用 |
|------|------|
| `EbbinghausScheduler.kt` | `object` 单例,`computeNextReview(current, outcome, now): WordProgress` |
| `WordSelector.kt` | `object` 单例,`pickNext(cards, now): StudyCard?` |

## 艾宾浩斯算法(大白话)
- **新词(stage=0)+ KNOWN** → stage=1, 1 天后复习
- **新词 + FORGOTTEN** → 保持 stage=0, interval=0(立即重出)
- **stage>=1 + KNOWN** → stage++, 间隔按 `[1, 2, 4, 7, 15]` 天,超 5 后翻倍,封顶 **60 天**
- **stage>=1 + FORGOTTEN** → 重置 stage=0
- `lastReviewedAt = now`(永远)
- `correctCount` / `wrongCount` 按 outcome 自增

## 抽词策略(WordSelector.pickNext)
1. **到期**(`isDue == true`)→ 随机抽一个
2. 否则**新词**(stage=0)→ 随机抽一个
3. 否则按 `nextReviewAt` 升序,取最早到期的
4. 空列表 → 返回 `null`

## 关键常量
- `BASE_INTERVALS = listOf(1L, 2L, 4L, 7L, 15L)` — **Long 数组**,不能改 Int!
- `MAX_INTERVAL_DAYS = 60L`
- `DAY_MILLIS = 24 * 60 * 60 * 1000L`

## 踩坑(避免重蹈覆辙)
- ⚠️ `BASE_INTERVALS` 一开始被写成 `List<Int>`,导致 `Pair<Int, Any>` 推导出 `Nothing`,编译失败 → 改成 `List<Long>`
- ⚠️ `now` 永远用参数传入,**不要**在逻辑层调 `System.currentTimeMillis()`,否则无法测试

## 记忆类型契约(供 data 层 / viewmodel 层对齐)
```kotlin
enum class ReviewOutcome { KNOWN, FORGOTTEN }
data class StudyCard(val word: Word, val progress: WordProgress) {
    val isDue: Boolean get() = progress.nextReviewAt <= System.currentTimeMillis() && progress.stage > 0
}
```