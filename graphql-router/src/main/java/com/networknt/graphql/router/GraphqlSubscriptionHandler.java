package com.networknt.graphql.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.networknt.config.Config;
import com.networknt.graphql.utils.QueryParameters;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xnio.IoUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Created by Nicholas Azar on October 18, 2017.
 */
public class GraphqlSubscriptionHandler implements WebSocketConnectionCallback {
    Logger logger = LoggerFactory.getLogger(GraphqlSubscriptionHandler.class);
    @Override
    public void onConnect(WebSocketHttpExchange webSocketHttpExchange, WebSocketChannel webSocketChannel) {
        webSocketHttpExchange.setResponseHeader("Sec-WebSocket-Protocol", "graphql-ws");
        webSocketChannel.getReceiveSetter().set(new AbstractReceiveListener() {

            @Override
            protected void onError(WebSocketChannel channel, Throwable error) {
                super.onError(channel, error);
            }

            @Override
            protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
                super.onClose(webSocketChannel, channel);
            }


            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                logger.debug("message = " + message);

                Map inputData = Config.getInstance().getMapper().readValue(message.getData(), Map.class);
                Map<String, String> outputData = new HashMap<>();

                if ("connection_init".equals(inputData.get("type"))) {
                    outputData.put("type", "connection_ack");
                } else if ("start".equals(inputData.get("type"))) {
                    QueryParameters parameters = QueryParameters.from(inputData);
                    ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                            .query(parameters.getQuery())
                            .variables(parameters.getVariables())
                            .operationName(parameters.getOperationName())
                            .build();
                    Instrumentation instrumentation = new ChainedInstrumentation(Collections.singletonList(new TracingInstrumentation()));
                    CompletableFuture<ExecutionResult> executionResult = GraphQL.newGraphQL(GraphqlPostHandler.schema)
                            .instrumentation(instrumentation).build()
                            .executeAsync(executionInput);

                    executionResult.thenAccept(executionResult1 -> {
                        try {
                            WebSockets.sendText(Config.getInstance().getMapper().writeValueAsString(executionResult1.getData()), channel, null);
                        } catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    });

                    outputData.put("type", "subscription_start");
                }
                WebSockets.sendText(Config.getInstance().getMapper().writeValueAsString(outputData), channel, null);
                channel.resumeReceives();



//


            }
        });
        webSocketChannel.resumeReceives();
    }
}
