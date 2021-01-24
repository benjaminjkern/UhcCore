const E = (a, b) => 1 / (1 + 10 ** ((b - a) / 400));

const EtoR = (x) => E(x, 1000) * 100;
const RtoE = (x) => 1000 - 400 * Math.log(100 / x - 1) / Math.log(10);

const total = 100;

// let K = 2 * (1000 - RtoE(40)) / (total - 1);
// const K = 7.414368802344478;
// console.log(K);
const K = 13.899276855538119;
// const K = 7.4143688023444700441185;
// const K = 1.4229596691368194827377;

const updateForward = (input) => {
    if (input.length == 1) return input;
    const front = input.slice(0, -1);
    const back = input[input.length - 1];
    return [...updateForward(front.map(x => x + K * (1 - E(x, back)))), back + front.reduce((p, c) => p - K * E(back, c), 0)];
}

const updateEnd = (input) => input.map((x, i) => x + input.slice(0, i).reduce((p, c) => p - K * E(x, c), 0) + input.slice(i + 1, -1).reduce((p, c) => p + K * (1 - E(x, c)), 0));

// const total = 4;
// const game = [50, ...Array(total - 1).fill(50)];

let josh = 80;
const game = [...Array(20).fill(99.995)];
let count = 0;
while (josh < 99.995) {
    count++;
    game[0] = josh;
    josh = updateForward(game.map(RtoE)).map(EtoR)[0];
}
console.log(count)
    // const game = [
    //         78.43621239744901, 16.659632857404397,
    //         99.8476135220935, 92.73471138497534,
    //         95.75755288471977, 73.26579828093118,
    //         94.71002431472743, 32.06993168962647,
    //         42.54315659647683, 15.993510330882641,
    //         83.5869475899274, 67.24050192746014,
    //         69.71692801523757, 20.06012683584638,
    //         60.49328328290385, 79.30380743277952,
    //         41.44221878545612, 56.21701344747627,
    //         62.78885027078448, 12.872138941125568
    //     ]
    // console.log(game);

// const newUpdate = updateForward(game.map(RtoE)).map(EtoR);
// 929.5634963777275
// console.log(updateEnd(game.map(RtoE)).map(EtoR));
// console.log(newUpdate.slice(1).reduce((p, c) => p + c, 0) / (total - 1));
// console.log(newUpdate.slice(0, -1).reduce((p, c) => p + c, 0) / (total - 1));
// console.log(RtoE(80));