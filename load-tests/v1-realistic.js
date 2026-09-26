import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const purchased = new Counter('purchased');
const soldOut   = new Counter('sold_out');
const errors    = new Counter('errors');

export const options = {
    vus:      200,
    duration: '30s',
};

// normally distributed think time — mean 1s, std dev 0.3s, clamped to [0.3, 2.5]
function thinkTime() {
    // Box-Muller transform for normal distribution
    const u1 = Math.random();
    const u2 = Math.random();
    const z  = Math.sqrt(-2 * Math.log(u1)) * Math.cos(2 * Math.PI * u2);
    const t  = 1.0 + 0.3 * z;
    return Math.min(Math.max(t, 0.3), 2.5);
}

export default function () {
    const payload = JSON.stringify({
        personId:  `user-${__VU}-${__ITER}`,
        productId: 'p1',
        quantity:  1,
    });

    const res = http.post('http://localhost:8080/api/orders/order', payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    if (res.status === 200) {
        purchased.add(1);
    } else if (res.status === 409) {
        soldOut.add(1);
    } else {
        errors.add(1);
    }

    check(res, {
        'status is 200 or 409': (r) => r.status === 200 || r.status === 409,
    });

    sleep(thinkTime());
}
