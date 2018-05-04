/* Generated By:JJTree: Do not edit this line. ONamedParameter.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.OResult;
import com.arcadedb.sql.executor.OResultInternal;

import java.util.Map;

public class NamedParameter extends InputParameter {

  protected int    paramNumber;
  protected String paramName;

  public NamedParameter(int id) {
    super(id);
  }

  public NamedParameter(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String toString() {
    return ":" + paramName;
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    Object finalValue = bindFromInputParams(params);
    if (finalValue == this) {
      builder.append(":" + paramName);
    } else if (finalValue instanceof String) {
      builder.append("\"");
      builder.append(Expression.encode(finalValue.toString()));
      builder.append("\"");
    } else if (finalValue instanceof SimpleNode) {
      ((SimpleNode) finalValue).toString(params, builder);
    } else {
      builder.append(finalValue);
    }
  }

  public Object getValue(Map<Object, Object> params) {
    Object result = null;
    if (params != null) {
      String key = paramName;
      if (params.containsKey(":" + key)) {
        result = params.get(":" + key);
      } else {
        result = params.get(paramNumber);
      }
    }
    return result;
  }

  public Object bindFromInputParams(Map<Object, Object> params) {
    if (params != null) {
      String key = paramName;
      if (params.containsKey(key)) {
        return toParsedTree(params.get(key));
      }
      return toParsedTree(params.get(paramNumber));
    }
    return this;
  }

  @Override
  public NamedParameter copy() {
    NamedParameter result = new NamedParameter(-1);
    result.paramName = paramName;
    result.paramNumber = paramNumber;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    NamedParameter that = (NamedParameter) o;

    if (paramNumber != that.paramNumber)
      return false;
    if (paramName != null ? !paramName.equals(that.paramName) : that.paramName != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = paramNumber;
    result = 31 * result + (paramName != null ? paramName.hashCode() : 0);
    return result;
  }

  public OResult serialize() {
    OResultInternal result = (OResultInternal) super.serialize();
    result.setProperty("paramNumber", paramNumber);
    result.setProperty("paramName", paramName);
    return result;
  }

  public void deserialize(OResult fromResult) {
    paramNumber = fromResult.getProperty("paramNumber");
    paramName = fromResult.getProperty("paramName");
  }
}
/* JavaCC - OriginalChecksum=8a00a9cf51a15dd75202f6372257fc1c (do not edit this line) */