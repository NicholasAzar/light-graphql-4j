package com.networknt.graphql.router;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.networknt.config.Config;
import com.networknt.graphql.common.GraphqlConstants;
import com.networknt.graphql.common.GraphqlUtil;
import com.networknt.graphql.utils.ResponseHelper;
import com.networknt.status.Status;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;
import graphql.schema.GraphQLSchema;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;


/**
 * Created by steve on 24/03/17.
 */
public class GraphqlPostHandler implements HttpHandler {
    static final String STATUS_GRAPHQL_MISSING_QUERY = "ERR11502";

    static final Logger logger = LoggerFactory.getLogger(GraphqlPostHandler.class);

    static GraphQLSchema schema = null;

    static {
        // load GraphQL Schema with service loader. It should be defined in SchemaProvider
        final ServiceLoader<SchemaProvider> schemaLoaders = ServiceLoader.load(SchemaProvider.class);
        for (final SchemaProvider provider : schemaLoaders) {
            if (provider.getSchema() != null) {
                schema = provider.getSchema();
                break;
            }
        }
        if (schema == null) {
            logger.error("Unable to load GraphQL schema - no SchemaProvider implementation available in the classpath");
        }
    }

    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        // get the request parameters as a Map<String, Object>
        Map<String, Object> requestParameters = (Map<String, Object>) httpServerExchange.getAttachment(GraphqlUtil.GRAPHQL_PARAMS);
        if (logger.isDebugEnabled()) logger.debug("requestParameters: " + requestParameters);

        // If a request comes in without any params, we treat it as an introspection query.
        if (requestParameters == null) {
            this.handleIntrospectionQuery(httpServerExchange);
            return;
        }

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        String query = (String) requestParameters.get(GraphqlConstants.GraphqlRouterConstants.GRAPHQL_REQUEST_QUERY_KEY);
        // No query in a non-empty request is an error condition.
        if (query == null) {
            Status status = new Status(STATUS_GRAPHQL_MISSING_QUERY);
            httpServerExchange.setStatusCode(status.getStatusCode());
            httpServerExchange.getResponseSender().send(status.toString());
            return;
        }

        Map<String, Object> variables = (Map<String, Object>) requestParameters.get(GraphqlConstants.GraphqlRouterConstants.GRAPHQL_REQUEST_VARIABLES_KEY);
        if (variables == null) {
            variables = new HashMap<>();
        }
        String operationName = (String) requestParameters.get(GraphqlConstants.GraphqlRouterConstants.GRAPHQL_REQUEST_OP_NAME_KEY);
        ExecutionResult executionResult = graphQL.execute(ExecutionInput.newExecutionInput()
                .query(query)
                .operationName(operationName)
                .variables(variables)
                .context(httpServerExchange)
                .build());
        Map<String, Object> result = new HashMap<>();
        if (executionResult.getErrors().size() > 0) {
            result.put(GraphqlConstants.GraphqlRouterConstants.GRAPHQL_RESPONSE_ERROR_KEY, executionResult.getErrors());
            logger.error("Errors: {}", executionResult.getErrors());
        } else {
            result.put(GraphqlConstants.GraphqlRouterConstants.GRAPHQL_RESPONSE_DATA_KEY, executionResult.getData());
        }
        ResponseHelper.addResponseParams(httpServerExchange);
        httpServerExchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
    }

    /**
     * For introspection queries, we execute the built in query supplied in graphql-java without any parameters.
     *
     * @param httpServerExchange
     * @throws JsonProcessingException
     */
    private void handleIntrospectionQuery(HttpServerExchange httpServerExchange) throws JsonProcessingException {
        Map<String, Object> result = new HashMap<>();
        GraphQL graphQL = GraphQL.newGraphQL(schema).build();
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(IntrospectionQuery.INTROSPECTION_QUERY).build();
        ExecutionResult executionResult = graphQL.execute(executionInput);
        result.put(GraphqlConstants.GraphqlRouterConstants.GRAPHQL_RESPONSE_DATA_KEY, executionResult.getData());
        ResponseHelper.addResponseParams(httpServerExchange);
        httpServerExchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
    }
}
