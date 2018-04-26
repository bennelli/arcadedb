package com.arcadedb.sql.executor;

import com.arcadedb.exception.PTimeoutException;
import com.arcadedb.sql.parser.Expression;
import com.arcadedb.sql.parser.Identifier;

/**
 * Created by luigidellaquila on 03/08/16.
 */
public class GlobalLetExpressionStep extends AbstractExecutionStep {
  private final Identifier varname;
  private final Expression expression;

  boolean executed = false;

  public GlobalLetExpressionStep(Identifier varName, Expression expression, OCommandContext ctx, boolean profilingEnabled) {
    super(ctx, profilingEnabled);
    this.varname = varName;
    this.expression = expression;
  }

  @Override public OResultSet syncPull(OCommandContext ctx, int nRecords) throws PTimeoutException {
    getPrev().ifPresent(x -> x.syncPull(ctx, nRecords));
    calculate(ctx);
    return new OInternalResultSet();
  }

  private void calculate(OCommandContext ctx) {
    if (executed) {
      return;
    }
    Object value = expression.execute((OResult) null, ctx);
    ctx.setVariable(varname.getStringValue(), value);
    executed = true;
  }

  @Override public String prettyPrint(int depth, int indent) {
    String spaces = OExecutionStepInternal.getIndent(depth, indent);
    return spaces + "+ LET (once)\n" +
        spaces + "  " + varname + " = " + expression;
  }
}