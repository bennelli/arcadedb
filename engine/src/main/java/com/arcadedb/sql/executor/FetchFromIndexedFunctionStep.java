package com.arcadedb.sql.executor;

import com.arcadedb.database.PDocument;
import com.arcadedb.database.PRecord;
import com.arcadedb.exception.PCommandExecutionException;
import com.arcadedb.exception.PTimeoutException;
import com.arcadedb.sql.parser.BinaryCondition;
import com.arcadedb.sql.parser.FromClause;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Created by luigidellaquila on 06/08/16.
 */
public class FetchFromIndexedFunctionStep extends AbstractExecutionStep {
  private BinaryCondition functionCondition;
  private FromClause      queryTarget;

  private long cost = 0;
  //runtime
  Iterator<PRecord> fullResult = null;

  public FetchFromIndexedFunctionStep(BinaryCondition functionCondition, FromClause queryTarget, OCommandContext ctx,
      boolean profilingEnabled) {
    super(ctx, profilingEnabled);
    this.functionCondition = functionCondition;
    this.queryTarget = queryTarget;
  }

  @Override
  public OResultSet syncPull(OCommandContext ctx, int nRecords) throws PTimeoutException {
    getPrev().ifPresent(x -> x.syncPull(ctx, nRecords));
    init(ctx);

    return new OResultSet() {
      int localCount = 0;

      @Override
      public boolean hasNext() {
        if (localCount >= nRecords) {
          return false;
        }
        if (!fullResult.hasNext()) {
          return false;
        }
        return true;
      }

      @Override
      public OResult next() {
        long begin = profilingEnabled ? System.nanoTime() : 0;
        try {
          if (localCount >= nRecords) {
            throw new IllegalStateException();
          }
          if (!fullResult.hasNext()) {
            throw new IllegalStateException();
          }
          OResultInternal result = new OResultInternal();
          result.setElement((PDocument) fullResult.next().getRecord());
          localCount++;
          return result;
        } finally {
          if (profilingEnabled) {
            cost += (System.nanoTime() - begin);
          }
        }
      }

      @Override
      public void close() {

      }

      @Override
      public Optional<OExecutionPlan> getExecutionPlan() {
        return null;
      }

      @Override
      public Map<String, Long> getQueryStats() {
        return null;
      }
    };
  }

  private void init(OCommandContext ctx) {
    if (fullResult == null) {
      long begin = profilingEnabled ? System.nanoTime() : 0;
      try {
        fullResult = functionCondition.executeIndexedFunction(queryTarget, ctx).iterator();
      } finally {
        if (profilingEnabled) {
          cost += (System.nanoTime() - begin);
        }
      }
    }
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    String result =
        OExecutionStepInternal.getIndent(depth, indent) + "+ FETCH FROM INDEXED FUNCTION " + functionCondition.toString();
    if (profilingEnabled) {
      result += " (" + getCostFormatted() + ")";
    }
    return result;
  }

  @Override
  public void reset() {
    this.fullResult = null;
  }

  @Override
  public long getCost() {
    return cost;
  }

  @Override
  public OResult serialize() {
    OResultInternal result = OExecutionStepInternal.basicSerialize(this);
    result.setProperty("functionCondition", this.functionCondition.serialize());
    result.setProperty("queryTarget", this.queryTarget.serialize());

    return result;
  }

  @Override
  public void deserialize(OResult fromResult) {
    try {
      OExecutionStepInternal.basicDeserialize(fromResult, this);
      functionCondition = new BinaryCondition(-1);
      functionCondition.deserialize(fromResult.getProperty("functionCondition "));

      queryTarget = new FromClause(-1);
      queryTarget.deserialize(fromResult.getProperty("functionCondition "));

    } catch (Exception e) {
      throw new PCommandExecutionException(e);
    }
  }
}