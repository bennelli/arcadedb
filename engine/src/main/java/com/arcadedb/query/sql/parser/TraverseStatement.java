/*
 * Copyright 2021 Arcade Data Ltd
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/* Generated By:JJTree: Do not edit this line. OTraverseStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.query.sql.parser;

import com.arcadedb.database.Database;
import com.arcadedb.exception.CommandSQLParsingException;
import com.arcadedb.query.sql.executor.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TraverseStatement extends Statement {

  public enum Strategy {
    DEPTH_FIRST, BREADTH_FIRST
  }

  protected List<TraverseProjectionItem> projections = new ArrayList<TraverseProjectionItem>();

  protected FromClause target;

  protected WhereClause whileClause;

  protected Skip skip;

  protected Limit limit;

  protected Strategy strategy;

  protected PInteger maxDepth;

  public TraverseStatement(int id) {
    super(id);
  }

  public TraverseStatement(SqlParser p, int id) {
    super(p, id);
  }

  public void validate() throws CommandSQLParsingException {
//    for(OTraverseProjectionItem projection:projections) {
//
//        projection. validate();
//        if (projection.isExpand() && groupBy != null) {
//          throw new OCommandSQLParsingException("expand() cannot be used together with GROUP BY");
//        }
//
//    }
    if (target.getItem().getStatement() != null) {
      target.getItem().getStatement().validate();
    }
  }

  @Override
  public ResultSet execute(Database db, Object[] args, CommandContext parentCtx, boolean usePlanCache) {
    BasicCommandContext ctx = new BasicCommandContext();
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
    InternalExecutionPlan executionPlan = createExecutionPlan(ctx, false);

    return new LocalResultSet(executionPlan);
  }

  @Override
  public ResultSet execute(Database db, Map params, CommandContext parentCtx, boolean usePlanCache) {
    BasicCommandContext ctx = new BasicCommandContext();
    if (parentCtx != null) {
      ctx.setParentWithoutOverridingChild(parentCtx);
    }
    ctx.setDatabase(db);
    ctx.setInputParameters(params);
    InternalExecutionPlan executionPlan = createExecutionPlan(ctx, false);

    return new LocalResultSet(executionPlan);
  }

  public InternalExecutionPlan createExecutionPlan(CommandContext ctx, boolean enableProfiling) {
    OTraverseExecutionPlanner planner = new OTraverseExecutionPlanner(this);
    return planner.createExecutionPlan(ctx, enableProfiling);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("TRAVERSE ");
    boolean first = true;
    for (TraverseProjectionItem item : projections) {
      if (!first) {
        builder.append(", ");
      }
      item.toString(params, builder);
      first = false;
    }

    if (target != null) {
      builder.append(" FROM ");
      target.toString(params, builder);
    }

    if (maxDepth != null) {
      builder.append(" MAXDEPTH ");
      maxDepth.toString(params, builder);
    }

    if (whileClause != null) {
      builder.append(" WHILE ");
      whileClause.toString(params, builder);
    }

    if (limit != null) {
      builder.append(" ");
      limit.toString(params, builder);
    }

    if (strategy != null) {
      builder.append(" strategy ");
      switch (strategy) {
      case BREADTH_FIRST:
        builder.append("breadth_first");
        break;
      case DEPTH_FIRST:
        builder.append("depth_first");
        break;
      }
    }

  }

  @Override
  public Statement copy() {
    TraverseStatement result = new TraverseStatement(-1);
    result.projections = projections == null ? null : projections.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.target = target == null ? null : target.copy();
    result.whileClause = whileClause == null ? null : whileClause.copy();
    result.limit = limit == null ? null : limit.copy();
    result.strategy = strategy;
    result.maxDepth = maxDepth == null ? null : maxDepth.copy();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    TraverseStatement that = (TraverseStatement) o;

    if (projections != null ? !projections.equals(that.projections) : that.projections != null)
      return false;
    if (target != null ? !target.equals(that.target) : that.target != null)
      return false;
    if (whileClause != null ? !whileClause.equals(that.whileClause) : that.whileClause != null)
      return false;
    if (limit != null ? !limit.equals(that.limit) : that.limit != null)
      return false;
    if (strategy != that.strategy)
      return false;
    return maxDepth != null ? maxDepth.equals(that.maxDepth) : that.maxDepth == null;
  }

  @Override
  public int hashCode() {
    int result = projections != null ? projections.hashCode() : 0;
    result = 31 * result + (target != null ? target.hashCode() : 0);
    result = 31 * result + (whileClause != null ? whileClause.hashCode() : 0);
    result = 31 * result + (limit != null ? limit.hashCode() : 0);
    result = 31 * result + (strategy != null ? strategy.hashCode() : 0);
    result = 31 * result + (maxDepth != null ? maxDepth.hashCode() : 0);
    return result;
  }

  @Override
  public boolean isIdempotent() {
    return true;
  }

  public List<TraverseProjectionItem> getProjections() {
    return projections;
  }

  public void setProjections(List<TraverseProjectionItem> projections) {
    this.projections = projections;
  }

  public FromClause getTarget() {
    return target;
  }

  public void setTarget(FromClause target) {
    this.target = target;
  }

  public WhereClause getWhileClause() {
    return whileClause;
  }

  public void setWhileClause(WhereClause whileClause) {
    this.whileClause = whileClause;
  }

  public Limit getLimit() {
    return limit;
  }

  public void setLimit(Limit limit) {
    this.limit = limit;
  }

  public Strategy getStrategy() {
    return strategy;
  }

  public void setStrategy(Strategy strategy) {
    this.strategy = strategy;
  }

  public PInteger getMaxDepth() {
    return maxDepth;
  }

  public void setMaxDepth(PInteger maxDepth) {
    this.maxDepth = maxDepth;
  }

  public Skip getSkip() {
    return skip;
  }

  public void setSkip(Skip skip) {
    this.skip = skip;
  }
}
/* JavaCC - OriginalChecksum=47399a3a3d5a423768bbdc70ee957464 (do not edit this line) */