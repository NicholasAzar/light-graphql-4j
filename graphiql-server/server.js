
import express from 'express';
import {graphiqlExpress} from 'graphql-server-express';

import { execute, subscribe } from 'graphql';
import { createServer } from 'http';
import { SubscriptionServer } from 'subscriptions-transport-ws';
import {makeExecutableSchema} from "graphql-tools";

const PORT = 4000;
const server = express();

server.use(`/graphiql`, graphiqlExpress({
  endpointURL: 'http://localhost:8080/graphql',
  subscriptionsEndpoint: `ws://localhost:8080/subscriptions`
}));

const ws = createServer(server);

// Dummy typedefs and resolvers to pass schema validation.
const typeDefs = `
type Query {
  id: ID!
}
`;
const resolvers = {
    Query: {}
};
const schema = makeExecutableSchema({ typeDefs, resolvers });

ws.listen(PORT, () => {
  console.log(`GraphQL Server is now running on http://localhost:${PORT}`);

  // Set up the WebSocket for handling GraphQL subscriptions
  new SubscriptionServer({execute, subscribe, schema}, {server: ws, path: '/subscriptions',});
});
