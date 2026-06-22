# viewmodel 层

## 模块职责
桥接 Repository 与 Compose UI,持有 UI 状态(`StateFlow`)。

## 关键文件
| 文件 | 作用 |
|------|------|
| `StudyUiState.kt` | UI 状态数据类:`currentCard, isFlipped, learnedToday, targetToday, isLoading` |
| `StudyViewModel.kt` | `AndroidViewModel`,持有 `MutableStateFlow<StudyUiState>`,暴露 `flipCard / markKnown / markForgotten` |

## 关键约定
- 通过 `(application as WordMasterApp).database` 拿到 Room,不要在 ViewModel 里 `WordMasterApp.getInstance()`
- `init { viewModelScope.launch { initializeProgress(); loadNext() } }`
- 每日目标 `targetToday = (totalWords / 5).coerceAtLeast(10)`
- `loadNext()` 从 `repository.observeStudyCards().first()` 取快照,避免持续订阅
- **不要**在这里写 UI 代码,UI 走 `viewmodel::method` 方法引用

## 踩坑
- ⚠️ `_state.update { ... }` 来自 `kotlinx.coroutines.flow.update` 扩展,**必须 import**
- ⚠️ 原 UI Agent 写过 `abstract class StudyViewModel` + `MockStudyViewModel`,Phase 2 已删除,只剩 concrete class

## 与 UI 的接口
```kotlin
@Composable
fun StudyScreen(viewModel: StudyViewModel) {
    val state by viewModel.state.collectAsState()
    // ... 用 state.currentCard / state.isFlipped / state.learnedToday
}
```