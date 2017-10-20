package com.networknt.graphql.router;

import com.networknt.config.Config;
import com.networknt.graphql.utils.ResponseHelper;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;
import graphql.schema.GraphQLSchema;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Created by Nicholas Azar on October 18, 2017.
 */
public class GraphqlSchemaHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(GraphqlSchemaHandler.class);

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

    /**
     * Handle introspection queries.
     *
     * @param httpServerExchange
     * @throws Exception
     */
    @Override
    public void handleRequest(HttpServerExchange httpServerExchange) throws Exception {
        Map<String, Object> result = new HashMap<>();
        GraphQL graphQL = GraphQL.newGraphQL(GraphqlSchemaHandler.schema).build();
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
                .query(IntrospectionQuery.INTROSPECTION_QUERY).build();
        ExecutionResult executionResult = graphQL.execute(executionInput);
        result.put("data", executionResult.getData());
        ResponseHelper.addResponseParams(httpServerExchange);
        httpServerExchange.getResponseSender().send(Config.getInstance().getMapper().writeValueAsString(result));
    }
}
