/* Generated By:JJTree: Do not edit this line. ODeleteEdgeFromToStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

public
class ODeleteEdgeFromToStatement extends ODeleteEdgeStatement {
  public ODeleteEdgeFromToStatement(int id) {
    super(id);
  }

  public ODeleteEdgeFromToStatement(OrientSql p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(OrientSqlVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override public ODeleteEdgeStatement copy() {
    return super.copy();
  }
}
/* JavaCC - OriginalChecksum=ca4781ee373b544b84bd6be28dba3ad5 (do not edit this line) */
