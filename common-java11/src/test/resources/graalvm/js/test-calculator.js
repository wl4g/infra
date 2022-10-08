function fibonacci(num) {
    if (num <= 1) return 1;
    return fibonacci(num - 1) + fibonacci(num - 2);
}

function squareRoot(num) {
    if (num < 0) return NaN;

    console.log("Testing for JSON.stringify() => ", JSON.stringify({ name: 'jack01' }));
    console.log("Testing for Math.pow()       => ", Math.pow(4, 4));
    console.log("Testing for parseInt         => ", parseInt("12345"));

    // ReferenceError: document is not defined
    try {
        console.log("Testing for document     => ", document);
    } catch (e) { console.error(e); }

    // ReferenceError: window is not defined
    try {
        console.log("Testing for window       => ", window);
    } catch (e) { console.error(e); }

    // ReferenceError: URL is not defined
    try {
        console.log("Testing for URL              => ", new URL("https://www.google.com/a/b/c").origin);
    } catch (e) { console.error(e); }

    return Math.sqrt(num);
}