# ui 层(Compose)

## 模块职责
Jetpack Compose 声明式 UI,Material3 主题,像素级交互。

## 结构
```
ui/
├── theme/        # Color / Type / Theme.kt(墨绿 + 米白配色,@Composable WordMasterTheme)
├── components/   # 通用可复用组件
│   ├── FlipCard.kt       # 3D 翻转动画卡片
│   ├── ProgressRing.kt   # Canvas 圆环进度
│   └── ControlButtons.kt # 双行按钮(导航 + 翻转 / 记住了 + 忘了)
└── screen/
    └── StudyScreen.kt    # 学习主屏(顶部环 / 中间卡 / 底部按钮)
```

## 关键约定
- `@Composable` 函数**必须 PascalCase**(Compose 官方约定),`.editorconfig` 已配 ktlint 跳过
- `Modifier.weight(1f)` 只在 `ColumnScope` / `RowScope` 内调用,顶层 Box **不能**用
- 状态收集:`val state by viewModel.state.collectAsState()`
- 主题切换:`WordMasterTheme(darkTheme = isSystemInDarkTheme()) { ... }`

## 配色(主题常量)
- `Teal700` 主色 · `Teal500` 次色 · `Teal100` 浅
- `Ink900/700/500/300` 灰阶(文字)
- `Paper` 米白底色 · `Coral` 错误 · `Amber` 高亮

## Typography
`WordMasterTypography`:displayLarge(48sp 单词大字)、titleLarge(22sp 释义)、bodyLarge、labelLarge

## 卡片布局草图
```
┌──────────────────────────────────┐
│  ◐ 5/20  今日进度                │ ProgressRing
├──────────────────────────────────┤
│                                  │
│           apple                  │ FlipCard front(displayLarge)
│                                  │
├──────────────────────────────────┤
│  [← 上一词] [⟳ 翻转] [下一词 →]   │ ControlButtons row 1
│  [记住了 ✓]      [忘了 ✗]        │ row 2(翻转后显示)
└──────────────────────────────────┘
```

## 踩坑
- ⚠️ `FlipCard.kt` 早期在 Box 顶层调了 `.weight(1f)`,编译失败 → 删掉,改成由调用方 `StudyScreen` 通过 `modifier = Modifier.fillMaxWidth().weight(1f)` 传入