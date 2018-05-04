/* Generated By:JJTree: Do not edit this line. OLikeOperator.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.OMultiValue;
import com.arcadedb.sql.executor.OQueryHelper;

public class LikeOperator extends SimpleNode implements BinaryCompareOperator {
  public LikeOperator(int id) {
    super(id);
  }

  public LikeOperator(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean execute(Object iLeft, Object iRight) {
    if (OMultiValue.isMultiValue(iLeft) || OMultiValue.isMultiValue(iRight))
      return false;

    if (iLeft == null || iRight == null) {
      return false;
    }
    return OQueryHelper.like(iLeft.toString(), iRight.toString());
  }

  @Override
  public String toString() {
    return "LIKE";
  }

  @Override
  public boolean supportsBasicCalculation() {
    return true;
  }

  @Override
  public LikeOperator copy() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    return obj != null && obj.getClass().equals(this.getClass());
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
/* JavaCC - OriginalChecksum=16d302abf0f85b404e57b964606952ca (do not edit this line) */