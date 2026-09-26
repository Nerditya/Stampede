import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const purchased = new Counter('purchased');
const soldOut   = new Counter('sold_out');
const errors    = new Counter('errors');

export const options = {
    vus:      200,   // virtual users (concurrent)
    duration: '10s', // run for 10 seconds
};

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
}
