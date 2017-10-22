package com.networknt.graphql.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.networknt.config.Config;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Graphql clients can send GET or POST HTTP requests.  The spec does not make an explicit
 * distinction.  So you may need to handle both.  The following was tested using
 * a graphiql client tool found here : https://github.com/skevy/graphiql-app
 *
 * You should consider bundling graphiql in your application
 *
 * https://github.com/graphql/graphiql
 *
 * This outlines more information on how to handle parameters over http
 *
 * http://graphql.org/learn/serving-over-http/
 */
public class QueryParameters {

    private String query;
    private String operationName;
    private Map<String, Object> variables = Collections.emptyMap();

    public String getQuery() {
        return query;
    }

    public String getOperationName() {
        return operationName;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public static QueryParameters from(Map inputData) {
        QueryParameters parameters = new QueryParameters();
        Map<String, Object> payload = (Map)inputData.get("payload");
        parameters.query = (String)payload.get("query");
        parameters.variables = (Map)payload.get("variables");
        return parameters;
    }
}
