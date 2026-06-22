# data 层

## 模块职责
词条数据持久化与对外暴露。

## 三层结构
- **model/** — 纯 Kotlin `data class`,领域模型(无 Room 注解)
- **db/** — Room 实体/DAO/Database(持久化细节)
- **repository/** — 对 ViewModel 的单一入口,跨层 join

## 关键文件
| 文件 | 作用 |
|------|------|
| `model/Word.kt` | 词条:`id, word, meaning`(@Serializable,从 assets 加载) |
| `model/WordProgress.kt` | 学习进度:`wordId, stage, nextReviewAt, correctCount, wrongCount, lastReviewedAt` |
| `model/ReviewState.kt` | `ReviewOutcome{KNOWN, FORGOTTEN}` + `StudyCard(word, progress)` 合并类型 |
| `db/WordProgressEntity.kt` | Room 实体(主键 `wordId`),`toDomain()` / `fromDomain()` 转换 |
| `db/WordProgressDao.kt` | `upsert` / `observeAll: Flow` / `getDueWords(now)` |
| `db/WordDatabase.kt` | Room 单例,`getInstance(context)` 双重检查锁 |
| `repository/WordRepository.kt` | 加载 `assets/words.json`,`observeStudyCards()` Flow 合并 word+progress |

## 约定
- `model/` 文件**不加 Room 注解**(保持纯净)
- Entity ↔ Domain 转换走 `toDomain()` / `fromDomain()` companion 方法
- 所有 Flow 操作走 `observeAll()`,不要直接 query 单条
- `getDueWords(now)` 用 `now: Long` 参数,**禁止**在 DAO 内调 `System.currentTimeMillis()`

## 详见
- 算法细节 → `logic/CLAUDE.md`
- 完整类型契约 → `logic/CLAUDE.md#memory-types`