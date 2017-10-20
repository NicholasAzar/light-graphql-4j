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

    String query;
    String operationName;
    Map<String, Object> variables = Collections.emptyMap();

    public String getQuery() {
        return query;
    }

    public String getOperationName() {
        return operationName;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public static QueryParameters from(String queryMessage) {
        QueryParameters parameters = new QueryParameters();
        Map<String, Object> json;
        try {
            json = Config.getInstance().getMapper().readValue(queryMessage, new TypeReference<Map<String, Object>>(){});
            parameters.query = (String) json.get("query");
            parameters.operationName = (String) json.get("operationName");
            parameters.variables = getVariables(json.get("variables"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parameters;
    }

    public static QueryParameters from(Map inputData) {
        QueryParameters parameters = new QueryParameters();
        Map<String, Object> payload = (Map)inputData.get("payload");
        parameters.query = (String)payload.get("query");
        parameters.variables = (Map)payload.get("variables");
        return parameters;
    }


    private static Map<String, Object> getVariables(Object variables) throws IOException {
        if (variables instanceof Map) {
            Map<?, ?> inputVars = (Map) variables;
            Map<String, Object> vars = new HashMap<>();
            inputVars.forEach((k, v) -> vars.put(String.valueOf(k), v));
            return vars;
        }
        return Config.getInstance().getMapper().readValue(String.valueOf(variables), new TypeReference<Map<String, Object>>(){});
    }

}
