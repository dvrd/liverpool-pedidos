import http from "k6/http";
import { check, sleep } from "k6";

const base = __ENV.BASE_URL || "http://127.0.0.1:8080";
const profile = __ENV.PROFILE || "baseline";

const profiles = {
  baseline: [
    { duration: "10s", target: 8 },
    { duration: "20s", target: 8 },
    { duration: "10s", target: 0 },
  ],
  stress: [
    { duration: "15s", target: 10 },
    { duration: "15s", target: 20 },
    { duration: "15s", target: 40 },
    { duration: "10s", target: 0 },
  ],
  capacity: [
    { duration: "15s", target: 25 },
    { duration: "15s", target: 50 },
    { duration: "15s", target: 100 },
    { duration: "15s", target: 150 },
    { duration: "10s", target: 0 },
  ],
};

export const options = {
  scenarios: {
    desk: {
      executor: "ramping-vus",
      startVUs: 1,
      stages: profiles[profile] || profiles.baseline,
      gracefulRampDown: "5s",
    },
  },
  thresholds: {
    http_req_failed: [{ threshold: "rate<0.10", abortOnFail: true, delayAbortEval: "5s" }],
  },
};

export default function desk() {
  const list = http.get(`${base}/api/v1/orders?page=0&size=20`);
  check(list, { "orders 200": (response) => response.status === 200 });
  sleep(1);

  const suggestions = http.get(`${base}/api/v1/orders/suggestions?term=mar`);
  check(suggestions, { "suggestions 200": (response) => response.status === 200 });
  sleep(1);

  const health = http.get(`${base}/api/v1/health`);
  check(health, { "health 200": (response) => response.status === 200 });
  sleep(1);
}
