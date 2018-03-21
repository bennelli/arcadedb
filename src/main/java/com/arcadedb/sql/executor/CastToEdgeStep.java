package com.arcadedb.sql.executor;

import com.arcadedb.database.PEdge;
import com.arcadedb.exception.PCommandExecutionException;
import com.arcadedb.exception.PTimeoutException;

import java.util.Map;
import java.util.Optional;

/**
 * Created by luigidellaquila on 20/02/17.
 */
public class CastToEdgeStep extends AbstractExecutionStep {

  private long cost = 0;

  public CastToEdgeStep(OCommandContext ctx, boolean profilingEnabled) {
    super(ctx, profilingEnabled);
  }

  @Override
  public OResultSet syncPull(OCommandContext ctx, int nRecords) throws PTimeoutException {
    OResultSet upstream = getPrev().get().syncPull(ctx, nRecords);
    return new OResultSet() {

      @Override
      public boolean hasNext() {
        return upstream.hasNext();
      }

      @Override
      public OResult next() {
        OResult result = upstream.next();
        long begin = profilingEnabled ? System.nanoTime() : 0;
        try {
          if (result.getElement().orElse(null) instanceof PEdge) {
            return result;
          }
          if (result.isEdge()) {
            if (result instanceof OResultInternal) {
              ((OResultInternal) result).setElement(result.getElement().get());
            } else {
              OResultInternal r = new OResultInternal();
              r.setElement(result.getElement().get());
              result = r;
            }
          } else {
            throw new PCommandExecutionException("Current element is not a vertex: " + result);
          }
          return result;
        } finally {
          if (profilingEnabled) {
            cost += (System.nanoTime() - begin);
          }
        }
      }

      @Override
      public void close() {
        upstream.close();
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

  @Override
  public String prettyPrint(int depth, int indent) {
    String result = OExecutionStepInternal.getIndent(depth, indent) + "+ CAST TO EDGE";
    if (profilingEnabled) {
      result += " (" + getCostFormatted() + ")";
    }
    return result;
  }

  @Override
  public long getCost() {
    return cost;
  }
}
