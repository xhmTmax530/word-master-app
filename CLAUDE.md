# WordMaster — Android 背单词 APP

## 项目速览
- **包名**:`com.wordmaster.app` · **版本**:1.0.0 · **minSdk 24 / targetSdk 34**
- **技术栈**:Kotlin 2.0.21 + Jetpack Compose(BOM 2024.10.01)+ Room 2.6.1 + Material3
- **架构**:MVVM + Repository · **构建**:AGP 8.5.2 + Gradle 8.10.2(./gradlew 包装器)
- **当前状态**:v1.0.0 MVP 已交付(2026-06-22),500 词精简版词库 + 艾宾浩斯复习

## 目录层级
```
word-master-app/
├── app/src/main/
│   ├── java/com/wordmaster/app/
│   │   ├── MainActivity.kt          # 入口
│   │   ├── WordMasterApp.kt         # Application + Room 初始化
│   │   ├── data/{model,db,repository}/  # 数据层 → [子 CLAUDE.md](app/src/main/java/com/wordmaster/app/data/CLAUDE.md)
│   │   ├── logic/                   # 业务逻辑 → [子 CLAUDE.md](app/src/main/java/com/wordmaster/app/logic/CLAUDE.md)
│   │   ├── viewmodel/               # ViewModel → [子 CLAUDE.md](app/src/main/java/com/wordmaster/app/viewmodel/CLAUDE.md)
│   │   └── ui/{theme,components,screen}/  # Compose UI → [子 CLAUDE.md](app/src/main/java/com/wordmaster/app/ui/CLAUDE.md)
│   ├── assets/words.json            # 500 词词库 → [子 CLAUDE.md](app/src/main/assets/CLAUDE.md)
│   └── res/                         # 资源(图标、字符串、主题)
├── gradle/libs.versions.toml        # 版本目录(所有依赖单一来源)
├── .editorconfig                    # ktlint + 编辑器配置
└── local.properties                 # sdk.dir (gitignored)
```

## 关键命令
| 用途 | 命令 |
|------|------|
| 编译 | `ANDROID_HOME=$HOME/.android_sdk ./gradlew :app:assembleDebug` |
| 代码风格检查 | `ktlint "app/src/main/java/com/wordmaster/app/**/*.kt"` |
| 代码风格修复 | `ktlint -F "app/src/main/java/com/wordmaster/app/**/*.kt"` |
| JSON 格式化 | `prettier --write app/src/main/assets/words.json` |
| Gradle 验证 | `./gradlew --version`(确认包装器) |

## 偏好(用户设定)
- 多智能体协作:Data / Logic / UI / QA 四角色 worktree 并行
- 工具链**全局已装**:`/usr/local/bin/ktlint`(v1.3.1)、`prettier` v3.8.4、Android SDK API 34
- Agent 在 worktree 中无 `ktlint`/`git commit`/`./gradlew` 权限 → **Lead 兜底**
- 中文注释优先(KDoc 用中文)
- ktlint 允许 `@Composable` 函数 PascalCase(`.editorconfig` 已配)
- 不引入 Hilt/Dagger(MVP 阶段);Room 单例直接用 `companion object`

## 记忆架构
- **索引**:`~/.claude/projects/-home-xhm-word-master-app/memory/MEMORY.md`(每次启动加载)
- **短期记忆**`memory/short-term/` — 会话级,会清理
- **长期记忆**`memory/long-term/` — 重要事实,**不压缩**
- 索引只放一行钩子,真正内容按路径读

## 子 CLAUDE.md 索引
- [data 层](app/src/main/java/com/wordmaster/app/data/CLAUDE.md)
- [logic 层](app/src/main/java/com/wordmaster/app/logic/CLAUDE.md)
- [viewmodel 层](app/src/main/java/com/wordmaster/app/viewmodel/CLAUDE.md)
- [ui 层](app/src/main/java/com/wordmaster/app/ui/CLAUDE.md)
- [assets](app/src/main/assets/CLAUDE.md)

## 重要事实(详见记忆文件)
- v1.0.0 已交付:`app-debug.apk` 16MB,ktlint 0 errors,500 词唯一
- 艾宾浩斯算法:1/2/4/7/15 天递推,封顶 60 天,答错重置
- 抽词策略:到期 → 新词 → 最早到期
- Git:`main` 分支线性,HEAD `5b7f8f2`
- 详见 `memory/long-term/wordmaster-v1-architecture.md`