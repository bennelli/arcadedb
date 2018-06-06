/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.sql.function;

import com.arcadedb.exception.CommandExecutionException;
import com.arcadedb.sql.executor.SQLFunction;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by frank on 25/05/2017.
 */
public abstract class SQLFunctionFactoryTemplate implements SQLFunctionFactory {

  private final Map<String, Object> functions;

  public SQLFunctionFactoryTemplate() {
    functions = new HashMap<>();
  }

  protected void register(final SQLFunction function) {
    functions.put(function.getName().toLowerCase(Locale.ENGLISH), function);
  }

  protected void register(String name, Object function) {
    functions.put(name.toLowerCase(Locale.ENGLISH), function);
  }

  @Override
  public boolean hasFunction(final String name) {
    return functions.containsKey(name);
  }

  @Override
  public Set<String> getFunctionNames() {
    return functions.keySet();
  }

  @Override
  public SQLFunction createFunction(final String name) throws CommandExecutionException {
    final Object obj = functions.get(name);

    if (obj == null)
      throw new CommandExecutionException("Unknown function name :" + name);

    if (obj instanceof SQLFunction)
      return (SQLFunction) obj;
    else {
      // it's a class
      final Class<?> clazz = (Class<?>) obj;
      try {
        return (SQLFunction) clazz.newInstance();
      } catch (Exception e) {
        throw new CommandExecutionException("Error in creation of function " + name
            + "(). Probably there is not an empty constructor or the constructor generates errors", e);

      }
    }

  }

  public Map<String, Object> getFunctions() {
    return functions;
  }

}