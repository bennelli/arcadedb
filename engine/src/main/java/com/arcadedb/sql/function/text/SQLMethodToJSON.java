/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */
package com.arcadedb.sql.function.text;

import com.arcadedb.database.Document;
import com.arcadedb.database.Identifiable;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.MultiValue;
import com.arcadedb.sql.method.misc.OAbstractSQLMethod;
import org.json.JSONObject;

import java.util.Map;

/**
 * Converts a document in JSON string.
 *
 * @author Johann Sorel (Geomatys)
 * @author Luca Garulli (l.garulli--(at)--orientdb.com)
 */
public class SQLMethodToJSON extends OAbstractSQLMethod {

  public static final String NAME = "tojson";

  public SQLMethodToJSON() {
    super(NAME, 0, 0);
  }

  @Override
  public String getSyntax() {
    return "toJSON()";
  }

  @Override
  public Object execute( Object iThis, Identifiable iCurrentRecord, CommandContext iContext, Object ioResult,
      Object[] iParams) {
    if (iThis == null)
      return null;

    if (iThis instanceof Document) {

      return ((Document) iThis).toJSON();

    } else if (iThis instanceof Map) {

      return new JSONObject(iThis);

    } else if (MultiValue.isMultiValue(iThis)) {

      StringBuilder builder = new StringBuilder();
      builder.append("[");
      boolean first = true;
      for (Object o : MultiValue.getMultiValueIterable(iThis, false)) {
        if (!first) {
          builder.append(",");
        }
        builder.append(execute(o, iCurrentRecord, iContext, ioResult, iParams));
        first = false;
      }

      builder.append("]");
      return builder.toString();
    }
    return null;
  }
}