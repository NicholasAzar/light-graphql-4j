package com.networknt.graphql.router;

import com.networknt.graphql.common.GraphqlUtil;
import com.networknt.info.ServerInfoGetHandler;
import com.networknt.server.HandlerProvider;
import io.undertow.Handlers;
import io.undertow.server.HttpHandler;
import io.undertow.util.Methods;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.WebSocketExtension;
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

    @Override
    public HttpHandler getHandler() {
        ExtensionHandshake extensionHandshake = new PerMessageDeflateHandshake();
        WebSocketConnectionCallback webSocketConnectionCallback = new GraphqlSubscriptionHandler();
        Set<Handshake> handshakeSet = new HashSet<>();
        Set<String> subprotocols = new HashSet<>();
        subprotocols.add("graphql-ws");
        handshakeSet.add(new Hybi13Handshake(subprotocols, true));
        handshakeSet.add(new Hybi07Handshake(subprotocols, true));
        handshakeSet.add(new Hybi08Handshake(subprotocols, true));
        HttpHandler httpHandler = new WebSocketProtocolHandshakeHandler(handshakeSet, webSocketConnectionCallback).addExtension(extensionHandshake);

        return path()
                .addPrefixPath(GraphqlUtil.config.getPath(), new GraphqlPostHandler())
                .addPrefixPath(GraphqlUtil.config.getSubscriptionsPath(), httpHandler);

    }
}
