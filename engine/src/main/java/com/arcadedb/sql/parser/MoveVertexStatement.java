/* Generated By:JJTree: Do not edit this line. OMoveVertexStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.PDatabase;
import com.arcadedb.sql.executor.*;

import java.util.HashMap;
import java.util.Map;

public class MoveVertexStatement extends Statement {
  protected FromItem         source;
  protected Cluster          targetCluster;
  protected Identifier       targetClass;
  protected UpdateOperations updateOperations;
  protected Batch            batch;

  public MoveVertexStatement(int id) {
    super(id);
  }

  public MoveVertexStatement(SqlParser p, int id) {
    super(p, id);
  }


  @Override public OResultSet execute(PDatabase db, Object[] args, OCommandContext parentCtx) {
    Map<Object, Object> params = new HashMap<>();
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        params.put(i, args[i]);
      }
    }
    return execute(db, params, parentCtx);
  }

  @Override public OResultSet execute(PDatabase db, Map params, OCommandContext parentCtx) {
    OBasicCommandContext ctx = new OBasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    ctx.setInputParameters(params);
    OUpdateExecutionPlan executionPlan = createExecutionPlan(ctx, false);
    executionPlan.executeInternal();
    return new OLocalResultSet(executionPlan);
  }

  public OUpdateExecutionPlan createExecutionPlan(OCommandContext ctx, boolean enableProfiling) {
    OMoveVertexExecutionPlanner planner = new OMoveVertexExecutionPlanner(this);
    return planner.createExecutionPlan(ctx, enableProfiling);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("MOVE VERTEX ");
    source.toString(params, builder);
    builder.append(" TO ");
    if (targetCluster != null) {
      targetCluster.toString(params, builder);
    } else {
      builder.append("CLASS:");
      targetClass.toString(params, builder);
    }

    if (updateOperations != null) {
      builder.append(" ");
      updateOperations.toString(params, builder);
    }

    if (batch != null) {
      builder.append(" ");
      batch.toString(params, builder);
    }
  }

  @Override
  public MoveVertexStatement copy() {
    MoveVertexStatement result = new MoveVertexStatement(-1);
    result.source = source.copy();
    result.targetClass = targetClass == null ? null : targetClass.copy();
    result.targetCluster = targetCluster == null ? null : targetCluster.copy();
    result.updateOperations = updateOperations == null ? null : updateOperations.copy();
    result.batch = batch == null ? null : batch.copy();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    MoveVertexStatement that = (MoveVertexStatement) o;

    if (!source.equals(that.source))
      return false;
    if (targetCluster != null ? !targetCluster.equals(that.targetCluster) : that.targetCluster != null)
      return false;
    if (targetClass != null ? !targetClass.equals(that.targetClass) : that.targetClass != null)
      return false;
    if (updateOperations != null ? !updateOperations.equals(that.updateOperations) : that.updateOperations != null)
      return false;
    return batch != null ? batch.equals(that.batch) : that.batch == null;
  }

  @Override
  public int hashCode() {
    int result = source.hashCode();
    result = 31 * result + (targetCluster != null ? targetCluster.hashCode() : 0);
    result = 31 * result + (targetClass != null ? targetClass.hashCode() : 0);
    result = 31 * result + (updateOperations != null ? updateOperations.hashCode() : 0);
    result = 31 * result + (batch != null ? batch.hashCode() : 0);
    return result;
  }

  public FromItem getSource() {
    return source;
  }

  public void setSource(FromItem source) {
    this.source = source;
  }

  public Cluster getTargetCluster() {
    return targetCluster;
  }

  public void setTargetCluster(Cluster targetCluster) {
    this.targetCluster = targetCluster;
  }

  public Identifier getTargetClass() {
    return targetClass;
  }

  public void setTargetClass(Identifier targetClass) {
    this.targetClass = targetClass;
  }

  public UpdateOperations getUpdateOperations() {
    return updateOperations;
  }

  public void setUpdateOperations(UpdateOperations updateOperations) {
    this.updateOperations = updateOperations;
  }

  public Batch getBatch() {
    return batch;
  }

  public void setBatch(Batch batch) {
    this.batch = batch;
  }
}
/* JavaCC - OriginalChecksum=5cb0b9d3644fd28813ff615fe59d577d (do not edit this line) */