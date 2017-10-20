package com.networknt.graphql.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.config.Config;
import com.networknt.graphql.utils.QueryParameters;
import com.sun.deploy.security.WIExplorerBrowserAuthenticator;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Nicholas Azar on October 18, 2017.
 */
public class GraphqlSubscriptionHandler implements WebSocketConnectionCallback {

    @Override
    public void onConnect(WebSocketHttpExchange webSocketHttpExchange, WebSocketChannel webSocketChannel) {
        webSocketHttpExchange.setResponseHeader("Sec-WebSocket-Protocol", "graphql-ws");
        webSocketChannel.getReceiveSetter().set(new AbstractReceiveListener() {

            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                WebSockets.sendText(message.getData(), channel, null);
//                QueryParameters parameters = QueryParameters.from(message.getData());
//                ExecutionInput executionInput = ExecutionInput.newExecutionInput()
//                        .query(parameters.getQuery())
//                        .variables(parameters.getVariables())
//                        .operationName(parameters.getOperationName())
//                        .build();
//                Instrumentation instrumentation = new ChainedInstrumentation(Collections.singletonList(new TracingInstrumentation()));
//                ExecutionResult executionResult = GraphQL.newGraphQL(GraphqlSchemaHandler.schema)
//                        .instrumentation(instrumentation).build()
//                        .execute(executionInput);
//
//                Publisher<Object> stream = executionResult.getData();
//                stream.subscribe(new Subscriber<Object>() {
//                    private final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();
//                    @Override
//                    public void onSubscribe(Subscription subscription) {
//                        subscriptionRef.set(subscription);
//                        subscription.request(1);
//                    }
//
//                    @Override
//                    public void onNext(Object o) {
//                        try {
//                            WebSockets.sendText(Config.getInstance().getMapper().writeValueAsString(o), channel, null);
//                        } catch (JsonProcessingException e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable) {
//
//                    }
//
//                    @Override
//                    public void onComplete() {
//                    }
//                });
            }
        });
        webSocketChannel.resumeReceives();
    }
}
