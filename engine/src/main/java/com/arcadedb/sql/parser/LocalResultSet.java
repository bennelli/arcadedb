/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.sql.parser;

import com.arcadedb.exception.RecordNotFoundException;
import com.arcadedb.sql.executor.ExecutionPlan;
import com.arcadedb.sql.executor.InternalExecutionPlan;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by luigidellaquila on 07/07/16.
 */
public class LocalResultSet implements ResultSet {

  private       ResultSet             lastFetch = null;
  private final InternalExecutionPlan executionPlan;
  private       boolean               finished  = false;

  public LocalResultSet(InternalExecutionPlan executionPlan) {
    this.executionPlan = executionPlan;
    fetchNext();
  }

  private boolean fetchNext() {
    do {
      try {
        lastFetch = executionPlan.fetchNext(100000);
        if (!lastFetch.hasNext()) {
          finished = true;
          return false;
        }

        return true;

      } catch (RecordNotFoundException e) {
        // IGNORE, FETCH NEXT
      }
    } while (true);
  }

  @Override
  public boolean hasNext() {
    if (finished)
      return false;

    if (lastFetch.hasNext())
      return true;
    else
      return fetchNext();
  }

  @Override
  public Result next() {
    if (finished)
      throw new IllegalStateException();

    if (!lastFetch.hasNext()) {
      if (!fetchNext())
        throw new IllegalStateException();

    }
    return lastFetch.next();
  }

  @Override
  public void close() {
    executionPlan.close();
  }

  @Override
  public Optional<ExecutionPlan> getExecutionPlan() {
    return Optional.of(executionPlan);
  }

  @Override
  public String toString() {
    return "LocalResultSet(hasNext=" + hasNext() + ")";
  }

  /**
   * Prints the resultset content to a string. The resultset is completely browsed.
   */
  public String print() {
    final StringBuilder buffer = new StringBuilder();
    for (int i = 0; hasNext(); ++i) {
      if (i > 0)
        buffer.append("\n");
      buffer.append(i + ": " + next().toJSON());
    }
    return buffer.toString();
  }

  @Override
  public Map<String, Long> getQueryStats() {
    return new HashMap<>();//TODO
  }

}
