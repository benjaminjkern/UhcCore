let alpha = 0;
const R = (e, l = 0) => 100 / (1 + 10 ** ((1000 + alpha * l - e) / 400));
const E = (r, l = 0) => 1000 + l * alpha - 400 * Math.log10(100 / r - 1);
alpha = E(90, 0) - 1000;

console.log(E(50, 2));