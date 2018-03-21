/* Generated By:JJTree: Do not edit this line. OIfStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.orientechnologies.orient.core.command.OBasicCommandContext;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.ODatabase;
import com.orientechnologies.orient.core.sql.executor.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OIfStatement extends OStatement {
  protected OBooleanExpression expression;
  protected List<OStatement> statements     = new ArrayList<OStatement>();
  protected List<OStatement> elseStatements = new ArrayList<OStatement>();//TODO support ELSE in the SQL syntax

  public OIfStatement(int id) {
    super(id);
  }

  public OIfStatement(OrientSql p, int id) {
    super(p, id);
  }

  @Override public boolean isIdempotent() {
    for (OStatement stm : statements) {
      if (!stm.isIdempotent()) {
        return false;
      }
    }
    for (OStatement stm : elseStatements) {
      if (!stm.isIdempotent()) {
        return false;
      }
    }
    return true;
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
    OIfExecutionPlan executionPlan = createExecutionPlan(ctx, false);

    OExecutionStepInternal last = executionPlan.executeUntilReturn();
    if (isIdempotent()) {
      OSelectExecutionPlan finalPlan = new OSelectExecutionPlan(ctx);
      finalPlan.chain(last);
      return new OLocalResultSet(finalPlan);
    } else {
      OUpdateExecutionPlan finalPlan = new OUpdateExecutionPlan(ctx);
      finalPlan.chain(last);
      finalPlan.executeInternal();
      return new OLocalResultSet(finalPlan);
    }
  }

  @Override public OResultSet execute(ODatabase db, Map params, OCommandContext parentCtx) {
    OBasicCommandContext ctx = new OBasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    ctx.setInputParameters(params);
    OIfExecutionPlan executionPlan = createExecutionPlan(ctx, false);

    OExecutionStepInternal last = executionPlan.executeUntilReturn();
    if (isIdempotent()) {
      OSelectExecutionPlan finalPlan = new OSelectExecutionPlan(ctx);
      finalPlan.chain(last);
      return new OLocalResultSet(finalPlan);
    } else {
      OUpdateExecutionPlan finalPlan = new OUpdateExecutionPlan(ctx);
      finalPlan.chain(last);
      finalPlan.executeInternal();
      return new OLocalResultSet(finalPlan);
    }
  }

  @Override public OIfExecutionPlan createExecutionPlan(OCommandContext ctx, boolean enableProfiling) {

    OIfExecutionPlan plan = new OIfExecutionPlan(ctx);

    IfStep step = new IfStep(ctx, enableProfiling);
    step.setCondition(this.expression);
    plan.chain(step);

    OBasicCommandContext subCtx1 = new OBasicCommandContext();
    subCtx1.setParent(ctx);
    OScriptExecutionPlan positivePlan = new OScriptExecutionPlan(subCtx1);
    for (OStatement stm : statements) {
      positivePlan.chain(stm.createExecutionPlan(subCtx1, enableProfiling), enableProfiling);
    }
    step.setPositivePlan(positivePlan);
    if (elseStatements.size() > 0) {
      OBasicCommandContext subCtx2 = new OBasicCommandContext();
      subCtx2.setParent(ctx);
      OScriptExecutionPlan negativePlan = new OScriptExecutionPlan(subCtx2);
      for (OStatement stm : elseStatements) {
        negativePlan.chain(stm.createExecutionPlan(subCtx2, enableProfiling), enableProfiling);
      }
      step.setNegativePlan(negativePlan);
    }
    return plan;
  }

  @Override public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("IF(");
    expression.toString(params, builder);
    builder.append("){\n");
    for (OStatement stm : statements) {
      stm.toString(params, builder);
      builder.append(";\n");
    }
    builder.append("}");
    if (elseStatements.size() > 0) {
      builder.append("\nELSE {\n");
      for (OStatement stm : elseStatements) {
        stm.toString(params, builder);
        builder.append(";\n");
      }
      builder.append("}");

    }
  }

  @Override public OIfStatement copy() {
    OIfStatement result = new OIfStatement(-1);
    result.expression = expression == null ? null : expression.copy();
    result.statements = statements == null ? null : statements.stream().map(OStatement::copy).collect(Collectors.toList());
    result.elseStatements =
        elseStatements == null ? null : elseStatements.stream().map(OStatement::copy).collect(Collectors.toList());
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    OIfStatement that = (OIfStatement) o;

    if (expression != null ? !expression.equals(that.expression) : that.expression != null)
      return false;
    if (statements != null ? !statements.equals(that.statements) : that.statements != null)
      return false;
    if (elseStatements != null ? !elseStatements.equals(that.elseStatements) : that.elseStatements != null)
      return false;

    return true;
  }

  @Override public int hashCode() {
    int result = expression != null ? expression.hashCode() : 0;
    result = 31 * result + (statements != null ? statements.hashCode() : 0);
    result = 31 * result + (elseStatements != null ? elseStatements.hashCode() : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=a8cd4fb832a4f3b6e71bb1a12f8d8819 (do not edit this line) */
