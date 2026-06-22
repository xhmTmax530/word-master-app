# assets/

## 关键文件
- **`words.json`** — 500 词精简版词库,格式 `[{"id": 0, "word": "...", "meaning": "..."}]`
  - id 范围:`0..499` 无空缺
  - 排序:七年级(0-199)→ 八年级(200-349)→ 九年级(350-499)
  - **必须 500 个唯一词**(v1 修复过 `society` 重复 → 替换为 `opportunity`)

## 加载方式
通过 `WordRepository.words`(`context.assets.open("words.json").bufferedReader()`),kotlinx.serialization 解析为 `List<Word>`。

## 修改流程
1. 编辑 JSON
2. `prettier --write app/src/main/assets/words.json`(2 空格缩进)
3. **必须保证 500 条且无重复**:
   ```bash
   python3 -c "import json; d=json.load(open('app/src/main/assets/words.json')); from collections import Counter; c=Counter(w['word'] for w in d); assert len(d)==500 and all(v==1 for v in c.values()), 'data integrity check failed'"
   ```

## 扩展字段(留待下版本)
当前 schema 只含 `id, word, meaning`,后续可加 `phonetic, partOfSpeech, example, audioUrl`,需同步修改:
- `data/model/Word.kt`(加字段)
- `WordRepository` 解析逻辑
- 暂不影响 Room schema