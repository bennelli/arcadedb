package com.arcadedb.sql.executor;

import com.arcadedb.sql.parser.Skip;

/**
 * Created by luigidellaquila on 08/07/16.
 */
public class SkipExecutionStep extends AbstractExecutionStep {
  private final Skip skip;

  int skipped = 0;

  OResultSet lastFetch;
  private boolean finished;

  public SkipExecutionStep(Skip skip, OCommandContext ctx, boolean profilingEnabled) {
    super(ctx, profilingEnabled);
    this.skip = skip;
  }

  @Override public OResultSet syncPull(OCommandContext ctx, int nRecords) {
    if (finished == true) {
      return new OInternalResultSet();//empty
    }
    int skipValue = skip.getValue(ctx);
    while (skipped < skipValue) {
      //fetch and discard
      OResultSet rs = prev.get().syncPull(ctx, Math.min(100, skipValue - skipped));//fetch blocks of 100, at most
      if (!rs.hasNext()) {
        finished = true;
        return new OInternalResultSet();//empty
      }
      while (rs.hasNext()) {
        rs.next();
        skipped++;
      }
    }

    return prev.get().syncPull(ctx, nRecords);

  }

  @Override public void sendTimeout() {

  }

  @Override public void close() {
    prev.ifPresent(x -> x.close());
  }

  @Override public String prettyPrint(int depth, int indent) {
    return OExecutionStepInternal.getIndent(depth, indent) + "+ SKIP (" + skip.toString() + ")";
  }

}