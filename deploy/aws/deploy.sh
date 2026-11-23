#!/usr/bin/env bash
# Rebuild the public desk on this host from origin/main.
# MongoDB and the API stay on the Docker network. Only Caddy publishes 80 and 443.
set -euo pipefail

root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$root"
mkdir -p /var/log
exec >> /var/log/liverpool-deploy.log 2>&1

echo "deploy $(date -Is) $(git -c safe.directory="$root" rev-parse --short HEAD 2>/dev/null || echo unknown)"

git -c safe.directory="$root" fetch origin main
git -c safe.directory="$root" checkout -B main origin/main

compose=(docker compose -p liverpool -f docker-compose.public.yml)

"${compose[@]}" up -d mongo

mongo_ready=0
for _ in $(seq 1 30); do
  if "${compose[@]}" exec -T mongo mongosh \
    --username liverpool \
    --password liverpool_pedidos \
    --authenticationDatabase admin \
    --quiet \
    --eval 'db.adminCommand({ ping: 1 }).ok' | grep -q '^1$'; then
    mongo_ready=1
    break
  fi
  sleep 2
done
if [[ "$mongo_ready" != 1 ]]; then
  echo "mongo did not become ready" >&2
  exit 1
fi

"${compose[@]}" stop api web

"${compose[@]}" exec -T mongo mongosh \
  --username liverpool \
  --password liverpool_pedidos \
  --authenticationDatabase admin \
  --quiet \
  liverpool_pedidos < deploy/aws/migrate-english.js

"${compose[@]}" up -d --build
docker image prune -f >/dev/null

for _ in $(seq 1 40); do
  if "${compose[@]}" exec -T web node --input-type=module -e '
    const health = await fetch("http://api:8080/api/v1/health").then((response) => response.json());
    if (health.status !== "ok" || health.service !== "order-desk") {
      console.error(JSON.stringify(health));
      process.exit(1);
    }
    const page = await fetch("http://api:8080/api/v1/orders?size=1").then((response) => response.json());
    if (!Array.isArray(page.content) || typeof page.totalElements !== "number") {
      console.error(JSON.stringify(page));
      process.exit(1);
    }
    const first = page.content[0];
    console.log("health ok, orders " + page.totalElements + (first ? " first " + first.orderNumber : ""));
  '; then
    echo "deploy ready"
    exit 0
  fi
  sleep 5
done

echo "the new API did not become ready" >&2
exit 1
