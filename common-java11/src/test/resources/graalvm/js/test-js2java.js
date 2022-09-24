function process(context) {
    console.info("context: ", context);
    console.info("context.id: ", context.getId());
    console.info("context.type: ", context.getType());
    console.info("context.eventSource: ", context.getEventSource());
    console.info("context.eventSource.attributes: ", context.getEventSource().getAttributes());
    console.info("context.eventSource.attributes['objId']: ", context.getEventSource().getAttributes()["objId"]);

    // Post external services.
    //
    // fetch is defined ???
    //const rawResponse = await fetch('https://httpbin.org/post', {
    //    method: 'POST',
    //    headers: {
    //      'Accept': 'application/json',
    //      'Content-Type': 'application/json'
    //    },
    //    body: JSON.stringify({a: 1, b: 'Textual content'})
    //});
    //const result = await rawResponse.json();
    //console.log("result: ", result);
    //return "The request external services response result: " + result;

    // fetch is defined ???
    //return fetch('https://httpbin.org/post', {
    //    method: 'POST',
    //    headers: {
    //      'Accept': 'application/json',
    //      'Content-Type': 'application/json'
    //    },
    //    body: JSON.stringify({a: 1, b: 'Textual content'})
    //});

    //const RestClient = Java.extend(Java.type("com.wl4g.infra.common.remoting.RestClient"));
    //const restClient = new RestClient();
    //return restClient;

    const response = httpClient.get("http://httpbin.org/get");
    return "The request external services response result: " + response;
}