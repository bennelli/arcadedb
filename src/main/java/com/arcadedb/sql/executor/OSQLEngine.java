package com.arcadedb.sql.executor;

import com.arcadedb.database.PDatabaseImpl;
import com.arcadedb.sql.parser.Statement;
import com.arcadedb.sql.parser.OStatementCache;

public class OSQLEngine {
  public static OSQLEngine getInstance() {
    return null;
  }

  public OSQLFunction getFunction(String name) {
    return null;
  }

  public static OSQLMethod getMethod(String name) {
    return null;
  }

  public static Statement parse(String query, PDatabaseImpl pDatabase) {
    return OStatementCache.get(query, pDatabase);
  }
}