package com.arcadedb.sql.executor;

import com.arcadedb.exception.PCommandExecutionException;
import com.arcadedb.sql.parser.*;

/**
 * Created by luigidellaquila on 08/08/16.
 */
public class ODeleteVertexExecutionPlanner {

  private final FromClause  fromClause;
  private final WhereClause whereClause;
  private final boolean     returnBefore;
  private final Limit       limit;

  public ODeleteVertexExecutionPlanner(DeleteVertexStatement stm) {
    this.fromClause = stm.getFromClause() == null ? null : stm.getFromClause().copy();
    this.whereClause = stm.getWhereClause() == null ? null : stm.getWhereClause().copy();
    this.returnBefore = stm.isReturnBefore();
    this.limit = stm.getLimit() == null ? null : stm.getLimit();
  }

  public ODeleteExecutionPlan createExecutionPlan(OCommandContext ctx, boolean enableProfiling) {
    ODeleteExecutionPlan result = new ODeleteExecutionPlan(ctx);

    if (handleIndexAsTarget(result, fromClause.getItem().getIndex(), whereClause, ctx)) {
      if (limit != null) {
        throw new PCommandExecutionException("Cannot apply a LIMIT on a delete from index");
      }
      if (returnBefore) {
        throw new PCommandExecutionException("Cannot apply a RETURN BEFORE on a delete from index");
      }

    } else {
      handleTarget(result, ctx, this.fromClause, this.whereClause, enableProfiling);
      handleLimit(result, ctx, this.limit, enableProfiling);
    }
    handleCastToVertex(result, ctx, enableProfiling);
    handleDelete(result, ctx, enableProfiling);
    handleReturn(result, ctx, this.returnBefore, enableProfiling);
    return result;
  }

  private boolean handleIndexAsTarget(ODeleteExecutionPlan result, IndexIdentifier indexIdentifier, WhereClause whereClause,
      OCommandContext ctx) {
    if (indexIdentifier == null) {
      return false;
    }
    throw new PCommandExecutionException("DELETE VERTEX FROM INDEX is not supported");
  }

  private void handleDelete(ODeleteExecutionPlan result, OCommandContext ctx, boolean profilingEnabled) {
    result.chain(new DeleteStep(ctx, profilingEnabled));
  }

  private void handleUnsafe(ODeleteExecutionPlan result, OCommandContext ctx, boolean unsafe, boolean profilingEnabled) {
    if (!unsafe) {
      result.chain(new CheckSafeDeleteStep(ctx, profilingEnabled));
    }
  }

  private void handleReturn(ODeleteExecutionPlan result, OCommandContext ctx, boolean returnBefore, boolean profilingEnabled) {
    if (!returnBefore) {
      result.chain(new CountStep(ctx, profilingEnabled));
    }
  }

  private void handleLimit(OUpdateExecutionPlan plan, OCommandContext ctx, Limit limit, boolean profilingEnabled) {
    if (limit != null) {
      plan.chain(new LimitExecutionStep(limit, ctx, profilingEnabled));
    }
  }

  private void handleCastToVertex(ODeleteExecutionPlan plan, OCommandContext ctx, boolean profilingEnabled) {
    plan.chain(new CastToVertexStep(ctx, profilingEnabled));
  }

  private void handleTarget(OUpdateExecutionPlan result, OCommandContext ctx, FromClause target, WhereClause whereClause, boolean profilingEnabled) {
    SelectStatement sourceStatement = new SelectStatement(-1);
    sourceStatement.setTarget(target);
    sourceStatement.setWhereClause(whereClause);
    OSelectExecutionPlanner planner = new OSelectExecutionPlanner(sourceStatement);
    result.chain(new SubQueryStep(planner.createExecutionPlan(ctx, profilingEnabled), ctx, ctx, profilingEnabled));
  }
}