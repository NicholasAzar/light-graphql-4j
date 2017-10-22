package com.networknt.graphql.common;

/**
 * Created by Nicholas Azar on October 22, 2017.
 */
public class GraphqlConstants {

    public static class GraphqlRouterConstants {
        public static final String GRAPHQL_WS_SUBPROTOCOL = "graphql-ws";

        public static final String GRAPHQL_RESPONSE_DATA_KEY = "data";
        public static final String GRAPHQL_RESPONSE_ERROR_KEY = "errors";
        public static final String GRAPHQL_RESPONSE_PAYLOAD_KEY = "payload";

        public static final String GRAPHQL_REQUEST_QUERY_KEY = "query";
        public static final String GRAPHQL_REQUEST_VARIABLES_KEY = "variables";
        public static final String GRAPHQL_REQUEST_OP_NAME_KEY = "operationName";
    }

    public static class GraphqlSubscriptionConstants {
        public static final String GRAPHQL_REQ_TYPE_KEY = "type";
        public static final String GRAPHQL_OP_ID_KEY = "id";

        public static final String GRAPHQL_CONN_INIT_REQ_TYPE = "connection_init";
        public static final String GRAPHQL_CONN_ACK_RES_TYPE = "connection_ack";
        public static final String GRAPHQL_DATA_RES_TYPE = "data";

        public static final String GRAPHQL_START_REQ_TYPE = "start";
    }

}
