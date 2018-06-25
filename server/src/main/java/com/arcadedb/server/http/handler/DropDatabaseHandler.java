/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.server.http.handler;

import com.arcadedb.database.Database;
import com.arcadedb.server.http.HttpServer;
import io.undertow.server.HttpServerExchange;

public class DropDatabaseHandler extends DatabaseAbstractHandler {
  public DropDatabaseHandler(final HttpServer httpServer) {
    super(httpServer);
  }

  @Override
  public void execute(final HttpServerExchange exchange, final Database database) {
    database.drop();

    httpServer.getServer().removeDatabase( database.getName() );

    exchange.setStatusCode(200);
    exchange.getResponseSender().send("{ \"result\" : \"ok\"}");
  }
}