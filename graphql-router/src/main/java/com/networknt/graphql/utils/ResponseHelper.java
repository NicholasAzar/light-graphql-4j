package com.networknt.graphql.utils;

import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;

/**
 * Created by Nicholas Azar on October 19, 2017.
 */
public class ResponseHelper {

    public static void addResponseParams(HttpServerExchange httpServerExchange) {
        httpServerExchange.getResponseHeaders().add(new HttpString("Access-Control-Allow-Methods"), "GET,POST,OPTIONS");
        httpServerExchange.getResponseHeaders().add(new HttpString("Access-Control-Allow-Headers"), "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
        httpServerExchange.getResponseHeaders().add(new HttpString("Access-Control-Allow-Origin"), "*");
    }

}
