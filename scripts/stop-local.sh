#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"

if [[ -f "$ROOT/.local/web.pid" ]]; then
  kill "$(cat "$ROOT/.local/web.pid")" >/dev/null 2>&1 || true
  rm -f "$ROOT/.local/web.pid"
fi
if [[ -f "$ROOT/.local/api.pid" ]]; then
  kill "$(cat "$ROOT/.local/api.pid")" >/dev/null 2>&1 || true
  rm -f "$ROOT/.local/api.pid"
fi
if [[ -x "$ROOT/.tools/mongodb/bin/mongod" ]]; then
  "$ROOT/.tools/mongodb/bin/mongod" --dbpath "$ROOT/.local/mongo" --shutdown >/dev/null 2>&1 || true
fi
echo "Local processes stopped."
