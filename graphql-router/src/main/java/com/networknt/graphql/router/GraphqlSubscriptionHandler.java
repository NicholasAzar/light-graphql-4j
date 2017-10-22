package com.networknt.graphql.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.config.Config;
import com.networknt.graphql.utils.QueryParameters;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.execution.reactive.CompletionStageMappingPublisher;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import static com.networknt.graphql.common.GraphqlConstants.GraphqlSubscriptionConstants;
import static com.networknt.graphql.common.GraphqlConstants.GraphqlRouterConstants;

/**
 * Created by Nicholas Azar on October 18, 2017.
 */
public class GraphqlSubscriptionHandler implements WebSocketConnectionCallback {
    Logger logger = LoggerFactory.getLogger(GraphqlSubscriptionHandler.class);

    /**
     * Entered for every new subscription connection request to the server.
     *
     * @param webSocketHttpExchange
     * @param webSocketChannel
     */
    @Override
    public void onConnect(WebSocketHttpExchange webSocketHttpExchange, WebSocketChannel webSocketChannel) {
        webSocketChannel.getReceiveSetter().set(new AbstractReceiveListener() {

            @Override
            protected void onError(WebSocketChannel channel, Throwable error) {
                super.onError(channel, error);
            }

            @Override
            protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
                super.onClose(webSocketChannel, channel);
            }

            /**
             * Responsible for parsing the different types of requests and generating appropriate responses.
             *
             * @param channel
             * @param message
             * @throws IOException
             */
            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                logger.debug("Message = " + message);

                Map inputData = Config.getInstance().getMapper().readValue(message.getData(), Map.class);
                String requestType = (String)inputData.get(GraphqlSubscriptionConstants.GRAPHQL_REQ_TYPE_KEY);
                String operationId = (String) inputData.get(GraphqlSubscriptionConstants.GRAPHQL_OP_ID_KEY);

                if (GraphqlSubscriptionConstants.GRAPHQL_CONN_INIT_REQ_TYPE.equals(requestType)) {
                    sendConnectionAck(channel);
                } else if (GraphqlSubscriptionConstants.GRAPHQL_START_REQ_TYPE.equals(requestType)) {
                    ExecutionResult executionResult = getExecutionResult(inputData);
                    if (executionResult.getErrors() != null && executionResult.getErrors().size() > 0) {
                        sendResponse(channel, executionResult, operationId);
                    } else {
                        subscribeToResults(executionResult, channel, operationId);
                    }
                } else {
                    logger.warn("Unknown type: " + requestType);
                }
                channel.resumeReceives();
            }
        });
        webSocketChannel.resumeReceives();
    }

    private void subscribeToResults(ExecutionResult executionResult, WebSocketChannel channel, String id) {
        CompletionStageMappingPublisher<ExecutionResult, CompletionStage> mappingPublisher = executionResult.getData();

        mappingPublisher.subscribe(new Subscriber<ExecutionResult>() {
            private final AtomicReference<Subscription> subscriptionRef = new AtomicReference<>();

            @Override
            public void onSubscribe(Subscription subscription) {
                subscriptionRef.set(subscription);
                subscription.request(1);
            }

            @Override
            public void onNext(ExecutionResult nextExecutionResult) {
                sendResponse(channel, nextExecutionResult, id);
                subscriptionRef.get().request(1);
            }

            @Override
            public void onError(Throwable throwable) {
                logger.info("Subscription onError", throwable);
                subscriptionRef.get().cancel();
            }

            @Override
            public void onComplete() {
                logger.info("Subscription onComplete");
                subscriptionRef.get().cancel();
            }
        });
    }

    private ExecutionResult getExecutionResult(Map inputData) {
        QueryParameters parameters = QueryParameters.from(inputData);
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(parameters.getQuery())
                .variables(parameters.getVariables())
                .operationName(parameters.getOperationName())
                .build();
        Instrumentation instrumentation = new ChainedInstrumentation(Collections.singletonList(new TracingInstrumentation()));
        return GraphQL.newGraphQL(GraphqlPostHandler.schema)
                .instrumentation(instrumentation)
                .build()
                .execute(executionInput);
    }

    private void sendConnectionAck(WebSocketChannel channel) throws JsonProcessingException {
        Map<String, Object> outputData = new HashMap<>();
        outputData.put(GraphqlSubscriptionConstants.GRAPHQL_REQ_TYPE_KEY, GraphqlSubscriptionConstants.GRAPHQL_CONN_ACK_RES_TYPE);
        WebSockets.sendText(Config.getInstance().getMapper().writeValueAsString(outputData), channel, null);
    }

    private void sendResponse(WebSocketChannel channel, ExecutionResult executionResult, String id) {
        Map<String, Object> nextPayload = new HashMap<>();
        if (executionResult.getData() != null) {
            nextPayload.put(GraphqlRouterConstants.GRAPHQL_RESPONSE_DATA_KEY, executionResult.getData());
        }
        if (executionResult.getErrors() != null && executionResult.getErrors().size() > 0) {
            nextPayload.put(GraphqlRouterConstants.GRAPHQL_RESPONSE_ERROR_KEY, executionResult.getErrors());
        }

        Map<String, Object> result = new HashMap<>();
        result.put(GraphqlSubscriptionConstants.GRAPHQL_OP_ID_KEY, id);
        result.put(GraphqlSubscriptionConstants.GRAPHQL_REQ_TYPE_KEY, GraphqlSubscriptionConstants.GRAPHQL_DATA_RES_TYPE);
        result.put(GraphqlRouterConstants.GRAPHQL_RESPONSE_PAYLOAD_KEY, nextPayload);
        try {
            WebSockets.sendText(Config.getInstance().getMapper().writeValueAsString(result), channel, null);
        } catch (JsonProcessingException e) {
            logger.error("Error while processing data response", e);
        }
    }
}
