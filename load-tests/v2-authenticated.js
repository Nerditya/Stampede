import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = 'http://localhost:8080';
const USERS = ['user1@example.com', 'user2@example.com', 'user3@example.com', 'user4@example.com', 'user5@example.com'];
const PASSWORD = 'password';

const purchased = new Counter('purchased');
const soldOut = new Counter('sold_out');
const errors = new Counter('errors');

export const options = {
  vus: 50,
  duration: '15s',
  thresholds: {
    http_req_failed: ['rate<0.05'],
    'http_req_duration{status:200}': ['p(95)<250'],
  },
};

export function setup() {
  const tokens = [];

  for (const email of USERS) {
    const loginRes = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({ email, password: PASSWORD }),
      { headers: { 'Content-Type': 'application/json' } }
    );

    check(loginRes, {
      'login succeeded': (r) => r.status === 200,
    });

    if (loginRes.status !== 200) {
      throw new Error(`login failed for ${email}: ${loginRes.status} ${loginRes.body}`);
    }

    tokens.push(loginRes.json('accessToken'));
  }

  return { tokens };
}

export default function (data) {
  const token = data.tokens[(__VU - 1) % data.tokens.length];
  const payload = JSON.stringify({
    items: [{ productId: 'p1', quantity: 1 }],
  });

  const res = http.post(`${BASE_URL}/api/orders/order`, payload, {
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
  });

  if (res.status === 200) {
    purchased.add(1);
  } else if (res.status === 409) {
    soldOut.add(1);
  } else {
    errors.add(1);
  }

  check(res, {
    'order response is accepted': (r) => r.status === 200 || r.status === 409,
  });
}
