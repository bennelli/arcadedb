/* Generated By:JJTree: Do not edit this line. OUpdateRemoveItem.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.orientechnologies.common.collection.OMultiValue;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.sql.executor.OResultInternal;

import java.util.Map;

public class OUpdateRemoveItem extends SimpleNode {

  OExpression left;
  OExpression right;

  public OUpdateRemoveItem(int id) {
    super(id);
  }

  public OUpdateRemoveItem(OrientSql p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(OrientSqlVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    left.toString(params, builder);
    if (right != null) {
      builder.append(" = ");
      right.toString(params, builder);
    }
  }

  public OUpdateRemoveItem copy() {
    OUpdateRemoveItem result = new OUpdateRemoveItem(-1);
    result.left = left == null ? null : left.copy();
    result.right = right == null ? null : right.copy();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    OUpdateRemoveItem that = (OUpdateRemoveItem) o;

    if (left != null ? !left.equals(that.left) : that.left != null)
      return false;
    if (right != null ? !right.equals(that.right) : that.right != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = left != null ? left.hashCode() : 0;
    result = 31 * result + (right != null ? right.hashCode() : 0);
    return result;
  }

  public void applyUpdate(OResultInternal result, OCommandContext ctx) {
    if (right != null) {
      Object leftVal = left.execute(result, ctx);
      Object rightVal = right.execute(result, ctx);
      if (OMultiValue.isMultiValue(leftVal)) {
        OMultiValue.remove(leftVal, rightVal, false);
      }
    } else {
      left.applyRemove(result, ctx);
    }
  }
}
/* JavaCC - OriginalChecksum=72e240d3dc1196fdea69e8fdc2bd69ca (do not edit this line) */
