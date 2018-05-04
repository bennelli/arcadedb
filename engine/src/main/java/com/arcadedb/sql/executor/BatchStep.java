package com.arcadedb.sql.executor;

import com.arcadedb.database.PDatabase;
import com.arcadedb.exception.PTimeoutException;
import com.arcadedb.sql.parser.Batch;

import java.util.Map;
import java.util.Optional;

/**
 * Created by luigidellaquila on 14/02/17.
 */
public class BatchStep extends AbstractExecutionStep {
  Integer batchSize;

  int count = 0;

  public BatchStep(Batch batch, OCommandContext ctx, boolean profilingEnabled) {
    super(ctx, profilingEnabled);
    batchSize = batch.evaluate(ctx);
  }

  @Override
  public OResultSet syncPull(OCommandContext ctx, int nRecords) throws PTimeoutException {
    OResultSet prevResult = getPrev().get().syncPull(ctx, nRecords);
    return new OResultSet() {
      @Override
      public boolean hasNext() {
        return prevResult.hasNext();
      }

      @Override
      public OResult next() {
        OResult res = prevResult.next();
        if (count % batchSize == 0) {
          PDatabase db = ctx.getDatabase();
          if (db.getTransaction().isActive()) {
            db.commit();
            db.begin();
          }
        }
        count++;
        return res;
      }

      @Override
      public void close() {
        getPrev().get().close();
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
  public void reset() {
    this.count = 0;
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    String spaces = OExecutionStepInternal.getIndent(depth, indent);
    StringBuilder result = new StringBuilder();
    result.append(spaces);
    result.append("+ BATCH COMMIT EVERY " + batchSize);
    return result.toString();
  }
}