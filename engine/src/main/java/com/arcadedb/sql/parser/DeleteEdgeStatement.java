/* Generated By:JJTree: Do not edit this line. ODeleteEdgeStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.PDatabase;
import com.arcadedb.sql.executor.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DeleteEdgeStatement extends Statement {
  private static final Object unset = new Object();

  protected Identifier className;
  protected Identifier targetClusterName;

  protected Rid       rid;
  protected List<Rid> rids;

  protected Expression leftExpression;
  protected Expression rightExpression;

  protected WhereClause whereClause;

  protected Limit limit;
  protected Batch batch = null;

  public DeleteEdgeStatement(int id) {
    super(id);
  }

  public DeleteEdgeStatement(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }


  @Override public OResultSet execute(PDatabase db, Map params, OCommandContext parentCtx) {
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

  @Override public OResultSet execute(PDatabase db, Object[] args, OCommandContext parentCtx) {
    Map<Object, Object> params = new HashMap<>();
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        params.put(i, args[i]);
      }
    }
    return execute(db, params, parentCtx);
  }

  public ODeleteExecutionPlan createExecutionPlan(OCommandContext ctx, boolean enableProfiling) {
    ODeleteEdgeExecutionPlanner planner = new ODeleteEdgeExecutionPlanner(this);
    return planner.createExecutionPlan(ctx, enableProfiling);
  }


  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("DELETE EDGE");

    if (className != null) {
      builder.append(" ");
      className.toString(params, builder);
      if (targetClusterName != null) {
        builder.append(" CLUSTER ");
        targetClusterName.toString(params, builder);
      }
    }

    if (rid != null) {
      builder.append(" ");
      rid.toString(params, builder);
    }
    if (rids != null) {
      builder.append(" [");
      boolean first = true;
      for (Rid rid : rids) {
        if (!first) {
          builder.append(", ");
        }
        rid.toString(params, builder);
        first = false;
      }
      builder.append("]");
    }
    if(leftExpression!=null){
      builder.append(" FROM ");
      leftExpression.toString(params, builder);
    }
    if(rightExpression!=null){
      builder.append(" TO ");
      rightExpression.toString(params, builder);
    }

    if (whereClause != null) {
      builder.append(" WHERE ");
      whereClause.toString(params, builder);
    }

    if (limit != null) {
      limit.toString(params, builder);
    }
    if (batch != null) {
      batch.toString(params, builder);
    }
  }

  @Override public DeleteEdgeStatement copy() {
    DeleteEdgeStatement result = null;
    try {
      result = getClass().getConstructor(Integer.TYPE).newInstance(-1);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    result.className = className == null ? null : className.copy();
    result.targetClusterName = targetClusterName == null ? null : targetClusterName.copy();
    result.rid = rid == null ? null : rid.copy();
    result.rids = rids == null ? null : rids.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.leftExpression = leftExpression==null?null:leftExpression.copy();
    result.rightExpression = rightExpression==null?null:rightExpression.copy();
    result.whereClause = whereClause == null ? null : whereClause.copy();
    result.limit = limit == null ? null : limit.copy();
    result.batch = batch == null ? null : batch.copy();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DeleteEdgeStatement that = (DeleteEdgeStatement) o;

    if (className != null ? !className.equals(that.className) : that.className != null)
      return false;
    if (targetClusterName != null ? !targetClusterName.equals(that.targetClusterName) : that.targetClusterName != null)
      return false;
    if (rid != null ? !rid.equals(that.rid) : that.rid != null)
      return false;
    if (rids != null ? !rids.equals(that.rids) : that.rids != null)
      return false;
    if (leftExpression != null ? !leftExpression.equals(that.leftExpression) : that.leftExpression != null)
      return false;
    if (rightExpression != null ? !rightExpression.equals(that.rightExpression) : that.rightExpression != null)
      return false;
    if (whereClause != null ? !whereClause.equals(that.whereClause) : that.whereClause != null)
      return false;
    if (limit != null ? !limit.equals(that.limit) : that.limit != null)
      return false;
    if (batch != null ? !batch.equals(that.batch) : that.batch != null)
      return false;

    return true;
  }

  @Override public int hashCode() {
    int result = className != null ? className.hashCode() : 0;
    result = 31 * result + (targetClusterName != null ? targetClusterName.hashCode() : 0);
    result = 31 * result + (rid != null ? rid.hashCode() : 0);
    result = 31 * result + (rids != null ? rids.hashCode() : 0);
    result = 31 * result + (leftExpression != null ? leftExpression.hashCode() : 0);
    result = 31 * result + (rightExpression!= null ? rightExpression.hashCode() : 0);
    result = 31 * result + (whereClause != null ? whereClause.hashCode() : 0);
    result = 31 * result + (limit != null ? limit.hashCode() : 0);
    result = 31 * result + (batch != null ? batch.hashCode() : 0);
    return result;
  }

  public Identifier getClassName() {
    return className;
  }

  public void setClassName(Identifier className) {
    this.className = className;
  }

  public Identifier getTargetClusterName() {
    return targetClusterName;
  }

  public void setTargetClusterName(Identifier targetClusterName) {
    this.targetClusterName = targetClusterName;
  }

  public Rid getRid() {
    return rid;
  }

  public void setRid(Rid rid) {
    this.rid = rid;
  }

  public List<Rid> getRids() {
    return rids;
  }

  public void setRids(List<Rid> rids) {
    this.rids = rids;
  }

  public WhereClause getWhereClause() {
    return whereClause;
  }

  public void setWhereClause(WhereClause whereClause) {
    this.whereClause = whereClause;
  }

  public Limit getLimit() {
    return limit;
  }

  public void setLimit(Limit limit) {
    this.limit = limit;
  }

  public Batch getBatch() {
    return batch;
  }

  public void setBatch(Batch batch) {
    this.batch = batch;
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
}
/* JavaCC - OriginalChecksum=8f4c5bafa99572d7d87a5d0a2c7d55a7 (do not edit this line) */