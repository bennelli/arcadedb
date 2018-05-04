/* Generated By:JJTree: Do not edit this line. OCreateEdgeStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.PDatabase;
import com.arcadedb.sql.executor.*;

import java.util.HashMap;
import java.util.Map;

public class CreateEdgeStatement extends Statement {

  protected Identifier targetClass;
  protected Identifier targetClusterName;

  protected Expression leftExpression;

  protected Expression rightExpression;

  protected InsertBody body;
  protected Number     retry;
  protected Number     wait;
  protected Batch      batch;

  public CreateEdgeStatement(int id) {
    super(id);
  }

  public CreateEdgeStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override public OResultSet execute(PDatabase db, Object[] args, OCommandContext parentCtx) {
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
    OInsertExecutionPlan executionPlan = createExecutionPlan(ctx, false);
    executionPlan.executeInternal(targetClass.getStringValue());
    return new OLocalResultSet(executionPlan);
  }

  @Override public OResultSet execute(PDatabase db, Map params, OCommandContext parentCtx) {
    OBasicCommandContext ctx = new OBasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    ctx.setInputParameters(params);
    OInsertExecutionPlan executionPlan = createExecutionPlan(ctx, false);
    executionPlan.executeInternal(targetClass.getStringValue());
    return new OLocalResultSet(executionPlan);
  }

  public OInsertExecutionPlan createExecutionPlan(OCommandContext ctx, boolean enableProfiling) {
    OCreateEdgeExecutionPlanner planner = new OCreateEdgeExecutionPlanner(this);
    return planner.createExecutionPlan(ctx, enableProfiling);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("CREATE EDGE");
    if (targetClass != null) {
      builder.append(" ");
      targetClass.toString(params, builder);
      if (targetClusterName != null) {
        builder.append(" CLUSTER ");
        targetClusterName.toString(params, builder);
      }
    }
    builder.append(" FROM ");
    leftExpression.toString(params, builder);

    builder.append(" TO ");
    rightExpression.toString(params, builder);

    if (body != null) {
      builder.append(" ");
      body.toString(params, builder);
    }
    if (retry != null) {
      builder.append(" RETRY ");
      builder.append(retry);
    }
    if (wait != null) {
      builder.append(" WAIT ");
      builder.append(wait);
    }
    if (batch != null) {
      batch.toString(params, builder);
    }
  }

  @Override public CreateEdgeStatement copy() {
    CreateEdgeStatement result = null;
    try {
      result = getClass().getConstructor(Integer.TYPE).newInstance(-1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    result.targetClass = targetClass==null?null:targetClass.copy();
    result.targetClusterName = targetClusterName==null?null:targetClusterName.copy();

    result.leftExpression = leftExpression==null?null:leftExpression.copy();

    result.rightExpression = rightExpression==null?null:rightExpression.copy();

    result.body = body==null?null:body.copy();
    result.retry = retry;
    result.wait = wait;
    result.batch = batch==null?null:batch.copy();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    CreateEdgeStatement that = (CreateEdgeStatement) o;

    if (targetClass != null ? !targetClass.equals(that.targetClass) : that.targetClass != null)
      return false;
    if (targetClusterName != null ? !targetClusterName.equals(that.targetClusterName) : that.targetClusterName != null)
      return false;
    if (leftExpression != null ? !leftExpression.equals(that.leftExpression) : that.leftExpression != null)
      return false;
    if (rightExpression != null ? !rightExpression.equals(that.rightExpression) : that.rightExpression != null)
      return false;
    if (body != null ? !body.equals(that.body) : that.body != null)
      return false;
    if (retry != null ? !retry.equals(that.retry) : that.retry != null)
      return false;
    if (wait != null ? !wait.equals(that.wait) : that.wait != null)
      return false;
    if (batch != null ? !batch.equals(that.batch) : that.batch != null)
      return false;

    return true;
  }

  @Override public int hashCode() {
    int result = targetClass != null ? targetClass.hashCode() : 0;
    result = 31 * result + (targetClusterName != null ? targetClusterName.hashCode() : 0);
    result = 31 * result + (leftExpression != null ? leftExpression.hashCode() : 0);
    result = 31 * result + (rightExpression != null ? rightExpression.hashCode() : 0);
    result = 31 * result + (body != null ? body.hashCode() : 0);
    result = 31 * result + (retry != null ? retry.hashCode() : 0);
    result = 31 * result + (wait != null ? wait.hashCode() : 0);
    result = 31 * result + (batch != null ? batch.hashCode() : 0);
    return result;
  }

  public Identifier getTargetClass() {
    return targetClass;
  }

  public void setTargetClass(Identifier targetClass) {
    this.targetClass = targetClass;
  }

  public Identifier getTargetClusterName() {
    return targetClusterName;
  }

  public void setTargetClusterName(Identifier targetClusterName) {
    this.targetClusterName = targetClusterName;
  }

  public Expression getLeftExpression() {
    return leftExpression;
  }

  public void setLeftExpression(Expression leftExpression) {
    this.leftExpression = leftExpression;
  }

  public Expression getRightExpression() {
    return rightExpression;
  }

  public void setRightExpression(Expression rightExpression) {
    this.rightExpression = rightExpression;
  }

  public InsertBody getBody() {
    return body;
  }

  public void setBody(InsertBody body) {
    this.body = body;
  }

  public Number getRetry() {
    return retry;
  }

  public void setRetry(Number retry) {
    this.retry = retry;
  }

  public Number getWait() {
    return wait;
  }

  public void setWait(Number wait) {
    this.wait = wait;
  }

  public Batch getBatch() {
    return batch;
  }

  public void setBatch(Batch batch) {
    this.batch = batch;
  }
}
/* JavaCC - OriginalChecksum=2d3dc5693940ffa520146f8f7f505128 (do not edit this line) */