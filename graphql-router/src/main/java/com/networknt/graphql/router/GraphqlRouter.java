package com.networknt.graphql.router;

import com.networknt.graphql.common.GraphqlConstants;
import com.networknt.graphql.common.GraphqlUtil;
import com.networknt.server.HandlerProvider;
import io.undertow.server.HttpHandler;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.WebSocketProtocolHandshakeHandler;
import io.undertow.websockets.core.protocol.Handshake;
import io.undertow.websockets.core.protocol.version07.Hybi07Handshake;
import io.undertow.websockets.core.protocol.version08.Hybi08Handshake;
import io.undertow.websockets.core.protocol.version13.Hybi13Handshake;
import io.undertow.websockets.extensions.ExtensionHandshake;
import io.undertow.websockets.extensions.PerMessageDeflateHandshake;

import java.util.HashSet;
import java.util.Set;

import static io.undertow.Handlers.path;


/**
 * Created by stevehu on 2017-03-22.
 */
public class GraphqlRouter implements HandlerProvider {

    /**
     * Handle all request routing's to the server.
     *
     * @return
     */
    @Override
    public HttpHandler getHandler() {
        ExtensionHandshake extensionHandshake = new PerMessageDeflateHandshake();
        WebSocketConnectionCallback webSocketConnectionCallback = new GraphqlSubscriptionHandler();
        HttpHandler httpHandler = new WebSocketProtocolHandshakeHandler(buildHandshakeset(), webSocketConnectionCallback).addExtension(extensionHandshake);

        return path()
                .addPrefixPath(GraphqlUtil.config.getPath(), new GraphqlPostHandler())
                .addPrefixPath(GraphqlUtil.config.getSubscriptionsPath(), httpHandler);
    }

    /**
     * For meeting specification of the general websocket protocol, we are required to supply the supported subprotocols
     * when requested.
     * From testing in the graphiql implementation from graphql-server-express, they request the "graphql-ws", and we
     * must include it in the response.
     * @return
     */
    private Set<Handshake> buildHandshakeset() {
        Set<Handshake> handshakeSet = new HashSet<>();
        Set<String> subprotocols = new HashSet<>();
        subprotocols.add(GraphqlConstants.GraphqlRouterConstants.GRAPHQL_WS_SUBPROTOCOL);
        handshakeSet.add(new Hybi13Handshake(subprotocols, true));
        handshakeSet.add(new Hybi07Handshake(subprotocols, true));
        handshakeSet.add(new Hybi08Handshake(subprotocols, true));
        return handshakeSet;
    }
}
