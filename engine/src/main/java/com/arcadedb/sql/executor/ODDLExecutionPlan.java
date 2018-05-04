package com.arcadedb.sql.executor;

import com.arcadedb.exception.PCommandExecutionException;
import com.arcadedb.sql.parser.ODDLStatement;

import java.util.Collections;
import java.util.List;

/**
 * @author Luigi Dell'Aquila (l.dellaquila-(at)-orientdb.com)
 */
public class ODDLExecutionPlan implements OInternalExecutionPlan {

  private final ODDLStatement statement;
  OCommandContext ctx;

  boolean executed = false;

  public ODDLExecutionPlan(OCommandContext ctx, ODDLStatement stm) {
    this.ctx = ctx;
    this.statement = stm;
  }

  @Override
  public void close() {

  }

  @Override
  public OResultSet fetchNext(int n) {
    return null;
  }

  public void reset(OCommandContext ctx) {
    executed = false;
  }

  @Override
  public long getCost() {
    return 0;
  }

  @Override
  public boolean canBeCached() {
    return false;
  }

  public OResultSet executeInternal(OBasicCommandContext ctx) throws PCommandExecutionException {
    if (executed) {
      throw new PCommandExecutionException("Trying to execute a result-set twice. Please use reset()");
    }
    executed = true;
    OResultSet result = statement.executeDDL(this.ctx);
    if (result instanceof OInternalResultSet) {
      ((OInternalResultSet) result).plan = this;
    }
    return result;
  }

  @Override
  public List<OExecutionStep> getSteps() {
    return Collections.emptyList();
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    String spaces = OExecutionStepInternal.getIndent(depth, indent);
    StringBuilder result = new StringBuilder();
    result.append(spaces);
    result.append("+ DDL\n");
    result.append("  ");
    result.append(statement.toString());
    return result.toString();
  }

  @Override
  public OResult toResult() {
    OResultInternal result = new OResultInternal();
    result.setProperty("type", "DDLExecutionPlan");
    result.setProperty(JAVA_TYPE, getClass().getName());
    result.setProperty("stmText", statement.toString());
    result.setProperty("cost", getCost());
    result.setProperty("prettyPrint", prettyPrint(0, 2));
    return result;
  }
}