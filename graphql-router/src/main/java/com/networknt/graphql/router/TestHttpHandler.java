package com.networknt.graphql.router;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

/**
 * Created by Nicholas Azar on October 19, 2017.
 */
public class TestHttpHandler implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        httpServerExchange.getResponseHeaders().put(new HttpString("Sec-WebSocket-Protocol"), "graphql-ws");
    }
}
