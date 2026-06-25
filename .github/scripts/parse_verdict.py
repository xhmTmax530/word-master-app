"""Parse DeepSeek chat completion response and extract verdict text.

DeepSeek v4-flash model wraps JSON responses in markdown code fences like:
    ```json
    { ... }
    ```
This script strips those fences before extracting `choices[0].message.content`.

Usage:
    echo "$RESPONSE_BODY" | python3 parse_verdict.py
    # Exit 0 + stdout = verdict text on success
    # Exit 1 + stderr = parse error message on failure
"""
import sys
import json
import re


def main() -> int:
    raw = sys.stdin.read()
    try:
        data = json.loads(raw)
        content = data["choices"][0]["message"]["content"]
    except (json.JSONDecodeError, KeyError, IndexError) as e:
        print(f"PARSE_ERROR: failed to extract message: {e}", file=sys.stderr)
        return 1

    stripped = content.strip()
    stripped = re.sub(r"^```(?:json)?\s*\n?", "", stripped)
    stripped = re.sub(r"\n?```\s*$", "", stripped)
    print(stripped)
    return 0


if __name__ == "__main__":
    sys.exit(main())