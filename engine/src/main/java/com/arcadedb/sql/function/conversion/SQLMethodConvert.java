/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */
package com.arcadedb.sql.function.conversion;

import com.arcadedb.database.Identifiable;
import com.arcadedb.schema.Type;
import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.method.misc.OAbstractSQLMethod;
import com.arcadedb.utility.LogManager;

import java.util.Locale;

/**
 * Converts a value to another type in Java or OrientDB's supported types.
 *
 * @author Luca Garulli (l.garulli--(at)--orientdb.com)
 */
public class SQLMethodConvert extends OAbstractSQLMethod {

  public static final String NAME = "convert";

  public SQLMethodConvert() {
    super(NAME, 1, 1);
  }

  @Override
  public String getSyntax() {
    return "convert(<type>)";
  }

  @Override
  public Object execute( final Object iThis, final Identifiable iCurrentRecord,
      final CommandContext iContext, final Object ioResult, final Object[] iParams) {
    if (iThis == null || iParams[0] == null) {
      return null;
    }

    final String destType = iParams[0].toString();

    if (destType.contains(".")) {
      try {
        return Type.convert(iContext.getDatabase(), iThis, Class.forName(destType));
      } catch (ClassNotFoundException e) {
        LogManager.instance().error(this, "Class for destination type was not found", e);
      }
    } else {
      final Type orientType = Type.valueOf(destType.toUpperCase(Locale.ENGLISH));
      if (orientType != null) {
        return Type.convert(iContext.getDatabase(), iThis, orientType.getDefaultJavaType());
      }
    }

    return null;
  }
}