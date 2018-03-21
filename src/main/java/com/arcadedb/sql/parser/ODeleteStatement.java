/* Generated By:JJTree: Do not edit this line. ODeleteStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.orientechnologies.orient.core.command.OBasicCommandContext;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.sql.executor.ODeleteExecutionPlan;
import com.orientechnologies.orient.core.sql.executor.ODeleteExecutionPlanner;
import com.orientechnologies.orient.core.sql.executor.OResultSet;

import java.util.HashMap;
import java.util.Map;

public class ODeleteStatement extends OStatement {

  public    OFromClause  fromClause;
  protected OWhereClause whereClause;
  protected boolean returnBefore = false;
  protected OLimit  limit        = null;
  protected boolean unsafe       = false;

  public ODeleteStatement(int id) {
    super(id);
  }

  public ODeleteStatement(OrientSql p, int id) {
    super(p, id);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("DELETE FROM ");
    fromClause.toString(params, builder);
    if (returnBefore) {
      builder.append(" RETURN BEFORE");
    }
    if (whereClause != null) {
      builder.append(" WHERE ");
      whereClause.toString(params, builder);
    }
    if (limit != null) {
      limit.toString(params, builder);
    }
    if (unsafe) {
      builder.append(" UNSAFE");
    }
  }

  @Override public ODeleteStatement copy() {
    ODeleteStatement result = new ODeleteStatement(-1);
    result.fromClause = fromClause == null ? null : fromClause.copy();
    result.whereClause = whereClause == null ? null : whereClause.copy();
    result.returnBefore = returnBefore;
    result.limit = limit == null ? null : limit.copy();
    result.unsafe = unsafe;
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ODeleteStatement that = (ODeleteStatement) o;

    if (returnBefore != that.returnBefore)
      return false;
    if (unsafe != that.unsafe)
      return false;
    if (fromClause != null ? !fromClause.equals(that.fromClause) : that.fromClause != null)
      return false;
    if (whereClause != null ? !whereClause.equals(that.whereClause) : that.whereClause != null)
      return false;
    if (limit != null ? !limit.equals(that.limit) : that.limit != null)
      return false;

    return true;
  }

  @Override public int hashCode() {
    int result = fromClause != null ? fromClause.hashCode() : 0;
    result = 31 * result + (whereClause != null ? whereClause.hashCode() : 0);
    result = 31 * result + (returnBefore ? 1 : 0);
    result = 31 * result + (limit != null ? limit.hashCode() : 0);
    result = 31 * result + (unsafe ? 1 : 0);
    return result;
  }

  @Override public OResultSet execute(ODatabase db, Map params, OCommandContext parentCtx) {
    OBasicCommandContext ctx = new OBasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    ctx.setInputParameters(params);
    ODeleteExecutionPlan executionPlan = createExecutionPlan(ctx, false);
    executionPlan.executeInternal();
    return new OLocalResultSet(executionPlan);
  }

  @Override public OResultSet execute(ODatabase db, Object[] args, OCommandContext parentCtx) {
    OBasicCommandContext ctx = new OBasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    Map<Object, Object> params = new HashMap<>();
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        params.put(i, args[i]);
      }
    }
    ctx.setInputParameters(params);
    ODeleteExecutionPlan executionPlan = createExecutionPlan(ctx, false);
    executionPlan.executeInternal();
    return new OLocalResultSet(executionPlan);
  }

  public ODeleteExecutionPlan createExecutionPlan(OCommandContext ctx, boolean enableProfiling) {
    ODeleteExecutionPlanner planner = new ODeleteExecutionPlanner(this);
    return planner.createExecutionPlan(ctx, enableProfiling);
  }

  public OFromClause getFromClause() {
    return fromClause;
  }

  public OWhereClause getWhereClause() {
    return whereClause;
  }

  public boolean isReturnBefore() {
    return returnBefore;
  }

  public OLimit getLimit() {
    return limit;
  }

  public boolean isUnsafe() {
    return unsafe;
  }
}
/* JavaCC - OriginalChecksum=5fb4ca5ba648e6c9110f41d806206a6f (do not edit this line) */
