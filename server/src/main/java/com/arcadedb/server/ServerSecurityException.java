/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.server;

public class ServerSecurityException extends ServerException {
  public ServerSecurityException() {
  }

  public ServerSecurityException(String message) {
    super(message);
  }

  public ServerSecurityException(String message, Throwable cause) {
    super(message, cause);
  }

  public ServerSecurityException(Throwable cause) {
    super(cause);
  }

  public ServerSecurityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}