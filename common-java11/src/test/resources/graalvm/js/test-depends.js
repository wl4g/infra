function process(number) {
    console.log("Input args number:", number, "typeof:", (typeof number), ",typeof(parseInt):", (typeof parseInt(number)));
    for (var i = 0; i < number; i++) {
        console.log("The run for:", i);
    }
    console.log(CommonUtil.toUrlQueryParam("https://www.google.com/a/b/c?q=123").get('q'));
    return "ok, input args number is " + number;
}