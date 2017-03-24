/*
 * Copyright (c) 2016 Network New Technologies Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.networknt.graphql.validator;

import com.networknt.config.Config;
import com.networknt.handler.MiddlewareHandler;
import com.networknt.status.Status;
import com.networknt.utility.ModuleRegistry;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a validator middleware handler for GraphQL. It validate the following:
 *
 * 1. The path is /graphql
 * 2. Method must be get or post
 * 3. The query parameter is a valid GraphQL query
 * 4. The body is a valid GraphQL json body
 *
 * Created by steve on 01/09/16.
 *
 */
public class ValidatorHandler implements MiddlewareHandler {
    public static final String CONFIG_NAME = "validator";
    public static final String GRAPHQL_URI = "/graphql";

    static final String STATUS_GRAPHQL_INVALID_PATH = "ERR11500";
    static final String STATUS_GRAPHQL_INVALID_METHOD = "ERR11501";

    static final Logger logger = LoggerFactory.getLogger(ValidatorHandler.class);

    static ValidatorConfig config = (ValidatorConfig)Config.getInstance().getJsonObjectConfig(CONFIG_NAME, ValidatorConfig.class);

    private volatile HttpHandler next;

    public ValidatorHandler() {}

    @Override
    public void handleRequest(final HttpServerExchange exchange) throws Exception {
        String path = exchange.getRequestPath();
        if(!GRAPHQL_URI.equals(path)) {
            // invalid GraphQL path
            Status status = new Status(STATUS_GRAPHQL_INVALID_PATH, path);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }
        // verify the method is get or post.
        HttpString method = exchange.getRequestMethod();
        if(Methods.GET.equals(method)) {
            // validate query parameter
            //final Collection<String> queryParameterValues = exchange.getQueryParameters().get(queryParameter.getName());

        } else if(Methods.POST.equals(method)) {
            // validate json body

        } else {
            // invalid GraphQL method
            Status status = new Status(STATUS_GRAPHQL_INVALID_METHOD, method);
            exchange.setStatusCode(status.getStatusCode());
            exchange.getResponseSender().send(status.toString());
            return;
        }

        next.handleRequest(exchange);
    }

    @Override
    public HttpHandler getNext() {
        return next;
    }

    @Override
    public MiddlewareHandler setNext(final HttpHandler next) {
        Handlers.handlerNotNull(next);
        this.next = next;
        return this;
    }

    @Override
    public boolean isEnabled() {
        return config.isEnabled();
    }

    @Override
    public void register() {
        ModuleRegistry.registerModule(ValidatorHandler.class.getName(), Config.getInstance().getJsonMapConfigNoCache(CONFIG_NAME), null);
    }

}
