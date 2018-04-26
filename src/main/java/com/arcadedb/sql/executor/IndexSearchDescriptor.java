package com.arcadedb.sql.executor;

import com.arcadedb.index.PIndex;
import com.arcadedb.sql.parser.AndBlock;
import com.arcadedb.sql.parser.BinaryCompareOperator;
import com.arcadedb.sql.parser.BinaryCondition;
import com.arcadedb.sql.parser.BooleanExpression;

/**
 * Created by luigidellaquila on 26/07/16.
 */
public class IndexSearchDescriptor {
  protected PIndex            idx;
  protected AndBlock          keyCondition;
  protected BinaryCondition   additionalRangeCondition;
  protected BooleanExpression remainingCondition;

  public IndexSearchDescriptor(PIndex idx, AndBlock keyCondition, BinaryCondition additional,
      BooleanExpression remainingCondition) {
    this.idx = idx;
    this.keyCondition = keyCondition;
    this.additionalRangeCondition = additional;
    this.remainingCondition = remainingCondition;
  }

  public IndexSearchDescriptor() {

  }

  public int cost(OCommandContext ctx) {
    OQueryStats stats = OQueryStats.get(ctx.getDatabase());

    String indexName = idx.getName();
    int size = keyCondition.getSubBlocks().size();
    boolean range = false;
    BooleanExpression lastOp = keyCondition.getSubBlocks().get(keyCondition.getSubBlocks().size() - 1);
    if (lastOp instanceof BinaryCondition) {
      BinaryCompareOperator op = ((BinaryCondition) lastOp).getOperator();
      range = op.isRangeOperator();
    }

    long val = stats.getIndexStats(indexName, size, range, additionalRangeCondition != null);
    if (val >= 0) {
      return val > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) val;
    }
    return Integer.MAX_VALUE;
  }
}