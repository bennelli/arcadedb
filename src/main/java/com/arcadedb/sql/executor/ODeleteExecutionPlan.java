package com.arcadedb.sql.executor;

import com.orientechnologies.orient.core.command.OCommandContext;

/**
 * @author Luigi Dell'Aquila (l.dellaquila-(at)-orientdb.com)
 */
public class ODeleteExecutionPlan extends OUpdateExecutionPlan {

  public ODeleteExecutionPlan(OCommandContext ctx) {
    super(ctx);
  }

  @Override public OResult toResult() {
    OResultInternal res = (OResultInternal) super.toResult();
    res.setProperty("type", "DeleteExecutionPlan");
    return res;
  }

  @Override
  public boolean canBeCached() {
    return false;
  }
}

