/* Generated By:JJTree: Do not edit this line. OIfNotExists.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

public
class OIfNotExists extends SimpleNode {
  public OIfNotExists(int id) {
    super(id);
  }

  public OIfNotExists(OrientSql p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(OrientSqlVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=5990db905ac7259f864fa5c62f123bcc (do not edit this line) */
