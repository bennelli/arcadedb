/* Generated By:JJTree: Do not edit this line. OUpdatePutItem.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import java.util.Map;

public class UpdatePutItem extends SimpleNode {

  protected Identifier left;
  protected Expression key;
  protected Expression value;

  public UpdatePutItem(int id) {
    super(id);
  }

  public UpdatePutItem(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    left.toString(params, builder);
    builder.append(" = ");
    key.toString(params, builder);
    builder.append(", ");
    value.toString(params, builder);
  }

  public UpdatePutItem copy() {
    UpdatePutItem result = new UpdatePutItem(-1);
    result.left = left == null ? null : left.copy();
    result.key = key == null ? null : key.copy();
    result.value = value == null ? null : value.copy();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    UpdatePutItem that = (UpdatePutItem) o;

    if (left != null ? !left.equals(that.left) : that.left != null)
      return false;
    if (key != null ? !key.equals(that.key) : that.key != null)
      return false;
    if (value != null ? !value.equals(that.value) : that.value != null)
      return false;

    return true;
  }

  @Override public int hashCode() {
    int result = left != null ? left.hashCode() : 0;
    result = 31 * result + (key != null ? key.hashCode() : 0);
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=a38339c33ebf0a8b21e76ddb278f4958 (do not edit this line) */