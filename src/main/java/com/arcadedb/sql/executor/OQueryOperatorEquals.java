package com.arcadedb.sql.executor;

public class OQueryOperatorEquals {
  public static boolean equals(Object o, Object right) {
    if (o == null && right == null) {
      return true;
    }
    if (o == null) {
      return false;
    }
    return o.equals(right);
  }
}