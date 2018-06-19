/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.server.http;

import com.arcadedb.ContextConfiguration;
import com.arcadedb.GlobalConfiguration;
import com.arcadedb.serializer.JsonSerializer;
import com.arcadedb.server.ArcadeDBServer;
import com.arcadedb.server.ServerException;
import com.arcadedb.server.ServerPlugin;
import com.arcadedb.server.http.handler.*;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;

import java.net.BindException;
import java.util.logging.Level;

public class HttpServer implements ServerPlugin {
  private       Undertow       undertow;
  private       JsonSerializer jsonSerializer = new JsonSerializer();
  private final ArcadeDBServer server;

  public HttpServer(final ArcadeDBServer server) {
    this.server = server;
  }

  @Override
  public void stopService() {
    if (undertow != null)
      undertow.stop();
  }

  @Override
  public void configure(ArcadeDBServer arcadeDBServer, ContextConfiguration configuration) {
  }

  @Override
  public void startService() {
    final ContextConfiguration configuration = server.getConfiguration();

    final String host = configuration.getValueAsString(GlobalConfiguration.SERVER_HTTP_INCOMING_HOST);
    final boolean httpAutoIncrementPort = configuration.getValueAsBoolean(GlobalConfiguration.SERVER_HTTP_AUTOINCREMENT_PORT);
    int port = configuration.getValueAsInteger(GlobalConfiguration.SERVER_HTTP_INCOMING_PORT);

    server.log(this, Level.INFO, "- Starting HTTP Server (host=%s port=%d)...", host, port);

    final RoutingHandler routes = new RoutingHandler();
    routes.get("/query/{database}/{command}", new QueryHandler(this));
    routes.post("/sql/{database}/{command}", new SQLHandler(this));
    routes.get("/document/{database}/{rid}", new GetDocumentHandler(this));
    routes.post("/document/{database}", new CreateDocumentHandler(this));
    routes.post("/server", new HAServersHandler(this));
    routes.post("/create/{database}", new CreateDatabaseHandler(this));
    routes.post("/drop/{database}", new DropDatabaseHandler(this));

    do {
      try {
        undertow = Undertow.builder().addHttpListener(port, host).setHandler(routes).build();
        undertow.start();

        server.log(this, Level.INFO, "- HTTP Server started (host=%s port=%d)", host, port);
        break;

      } catch (Exception e) {
        undertow = null;

        if (e.getCause() instanceof BindException) {
          // RETRY
          server.log(this, Level.WARNING, "- HTTP Port %s not available", port);
          ++port;
          continue;
        }

        throw new ServerException("Error on starting HTTP Server", e);
      }
    } while (httpAutoIncrementPort);
  }

  public ArcadeDBServer getServer() {
    return server;
  }

  public JsonSerializer getJsonSerializer() {
    return jsonSerializer;
  }
}
