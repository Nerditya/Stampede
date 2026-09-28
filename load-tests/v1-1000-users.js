import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const purchased = new Counter('purchased');
const soldOut = new Counter('sold_out');
const errors = new Counter('errors');

export const options = {
    vus: 1000,
    duration: '10s',
};

http.setResponseCallback(http.expectedStatuses(200, 409));

export default function () {
    const payload = JSON.stringify({
        personId: `user-${__VU}-${__ITER}`,
        productId: 'p1',
        quantity: 1,
    });

    const response = http.post('http://localhost:8080/api/orders/order', payload, {
        headers: { 'Content-Type': 'application/json' },
    });

    if (response.status === 200) {
        purchased.add(1);
    } else if (response.status === 409) {
        soldOut.add(1);
    } else {
        errors.add(1);
    }

    check(response, {
        'status is 200 or 409': (result) => result.status === 200 || result.status === 409,
    });
}