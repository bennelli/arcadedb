package com.arcadedb.sql.executor;

import com.arcadedb.database.PDatabase;
import com.arcadedb.schema.PDocumentType;
import com.arcadedb.sql.parser.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by luigidellaquila on 08/08/16.
 */
public class OCreateEdgeExecutionPlanner {

  protected Identifier targetClass;
  protected Identifier targetClusterName;
  protected Expression leftExpression;
  protected Expression rightExpression;

  protected InsertBody body;
  protected Number     retry;
  protected Number     wait;
  protected Batch      batch;


  public OCreateEdgeExecutionPlanner(CreateEdgeStatement statement) {
    this.targetClass = statement.getTargetClass() == null ? null : statement.getTargetClass().copy();
    this.targetClusterName = statement.getTargetClusterName() == null ? null : statement.getTargetClusterName().copy();
    this.leftExpression = statement.getLeftExpression() == null ? null : statement.getLeftExpression().copy();
    this.rightExpression = statement.getRightExpression() == null ? null : statement.getRightExpression().copy();
    this.body = statement.getBody() == null ? null : statement.getBody().copy();
    this.retry = statement.getRetry();
    this.wait = statement.getWait();
    this.batch = statement.getBatch() == null ? null : statement.getBatch().copy();

  }

  public OInsertExecutionPlan createExecutionPlan(OCommandContext ctx, boolean enableProfiling) {

    if (targetClass == null) {
      if (targetClusterName == null) {
        targetClass = new Identifier("E");
      } else {
        PDatabase db = ctx.getDatabase();
        PDocumentType clazz = db.getSchema()
            .getTypeByBucketId((db.getSchema().getBucketByName(targetClusterName.getStringValue()).getId()));
        if (clazz != null) {
          targetClass = new Identifier(clazz.getName());
        } else {
          targetClass = new Identifier("E");
        }
      }
    }

    OInsertExecutionPlan result = new OInsertExecutionPlan(ctx);

    handleCheckType(result, ctx, enableProfiling);

    handleGlobalLet(result, new Identifier("$__ORIENT_CREATE_EDGE_fromV"), leftExpression, ctx, enableProfiling);
    handleGlobalLet(result, new Identifier("$__ORIENT_CREATE_EDGE_toV"), rightExpression, ctx, enableProfiling);

    result.chain(new CreateEdgesStep(targetClass, targetClusterName, new Identifier("$__ORIENT_CREATE_EDGE_fromV"),
        new Identifier("$__ORIENT_CREATE_EDGE_toV"), wait, retry, batch, ctx, enableProfiling));

    handleSetFields(result, body, ctx, enableProfiling);
    handleSave(result, targetClusterName, ctx, enableProfiling);
    //TODO implement batch, wait and retry
    return result;
  }

  private void handleGlobalLet(OInsertExecutionPlan result, Identifier name, Expression expression, OCommandContext ctx, boolean profilingEnabled) {
    result.chain(new GlobalLetExpressionStep(name, expression, ctx, profilingEnabled));
  }

  private void handleCheckType(OInsertExecutionPlan result, OCommandContext ctx, boolean profilingEnabled) {
    if (targetClass != null) {
      result.chain(new CheckClassTypeStep(targetClass.getStringValue(), "E", ctx, profilingEnabled));
    }
  }

  private void handleSave(OInsertExecutionPlan result, Identifier targetClusterName, OCommandContext ctx, boolean profilingEnabled) {
    result.chain(new SaveElementStep(ctx, targetClusterName, profilingEnabled));
  }

  private void handleSetFields(OInsertExecutionPlan result, InsertBody insertBody, OCommandContext ctx, boolean profilingEnabled) {
    if (insertBody == null) {
      return;
    }
    if (insertBody.getIdentifierList() != null) {
      result.chain(new InsertValuesStep(insertBody.getIdentifierList(), insertBody.getValueExpressions(), ctx, profilingEnabled));
    } else if (insertBody.getContent() != null) {
      result.chain(new UpdateContentStep(insertBody.getContent(), ctx, profilingEnabled));
    } else if (insertBody.getSetExpressions() != null) {
      List<UpdateItem> items = new ArrayList<>();
      for (InsertSetExpression exp : insertBody.getSetExpressions()) {
        UpdateItem item = new UpdateItem(-1);
        item.setOperator(UpdateItem.OPERATOR_EQ);
        item.setLeft(exp.getLeft().copy());
        item.setRight(exp.getRight().copy());
        items.add(item);
      }
      result.chain(new UpdateSetStep(items, ctx, profilingEnabled));
    }
  }

}