#!/usr/bin/env bash
# Starts MongoDB when the port is free, then the API and the desk.
# Meant for macOS with the binary in .tools/mongodb or with mongod already installed.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
mkdir -p "$ROOT/.local/mongo"

if ! nc -z 127.0.0.1 27017 >/dev/null 2>&1; then
  if [[ -x "$ROOT/.tools/mongodb/bin/mongod" ]]; then
    "$ROOT/.tools/mongodb/bin/mongod" \
      --dbpath "$ROOT/.local/mongo" \
      --bind_ip 127.0.0.1 \
      --port 27017 \
      --fork \
      --logpath "$ROOT/.local/mongod.log"
  else
    echo "Nothing is listening on port 27017 and .tools/mongodb/bin/mongod is missing."
    echo "Start MongoDB yourself or use: docker compose up --build"
    exit 1
  fi
fi

for _ in $(seq 1 50); do
  nc -z 127.0.0.1 27017 >/dev/null 2>&1 && break
  sleep 0.2
done

if [[ -z "${JAVA_HOME:-}" ]]; then
  JAVA_HOME="$(/usr/libexec/java_home -v 11)"
  export JAVA_HOME
fi

cd "$ROOT/backend"
nohup mvn -q spring-boot:run > "$ROOT/.local/api.log" 2>&1 &
echo $! > "$ROOT/.local/api.pid"

cd "$ROOT/frontend"
if [[ ! -d node_modules ]]; then
  npm install
fi
nohup npm run dev > "$ROOT/.local/web.log" 2>&1 &
echo $! > "$ROOT/.local/web.pid"

echo "MongoDB: mongodb://localhost:27017/liverpool_pedidos"
echo "API:     http://localhost:8080/swagger-ui.html"
echo "Desk:    http://localhost:3000"
echo "Logs:    .local/api.log and .local/web.log"
