/* Generated By:JJTree: Do not edit this line. OCreateClusterStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.PDatabase;
import com.arcadedb.exception.PCommandExecutionException;
import com.arcadedb.sql.executor.OCommandContext;
import com.arcadedb.sql.executor.OInternalResultSet;
import com.arcadedb.sql.executor.OResultInternal;
import com.arcadedb.sql.executor.OResultSet;

import java.util.Map;

public class CreateClusterStatement extends ODDLStatement {

  /**
   * Class name
   */
  protected Identifier name;

  protected boolean ifNotExists = false;

  protected PInteger id;

  protected boolean blob = false;

  public CreateClusterStatement(int id) {
    super(id);
  }

  public CreateClusterStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override
  public OResultSet executeDDL(OCommandContext ctx) {
    PDatabase db = ctx.getDatabase();
    int existingId = db.getSchema().getBucketByName(name.getStringValue()).getId();
    if (existingId >= 0) {
      if (ifNotExists) {
        return new OInternalResultSet();
      } else {
        throw new PCommandExecutionException("Cluster " + name.getStringValue() + " already exists");
      }
    }
    if (id != null) {
      String existingName = db.getSchema().getBucketById(id.getValue().intValue()).getName();
      if (existingName != null) {
        if (ifNotExists) {
          return new OInternalResultSet();
        } else {
          throw new PCommandExecutionException("Cluster " + id.getValue() + " already exists");
        }
      }
    }

    OResultInternal result = new OResultInternal();
    result.setProperty("operation", "create cluster");
    result.setProperty("clusterName", name.getStringValue());

    int requestedId = id == null ? -1 : id.getValue().intValue();
    int finalId = -1;
    if (blob) {
//      if (requestedId == -1) {
//        finalId = db.addBlobCluster(name.getStringValue());
//        result.setProperty("finalId", finalId);
//      } else {
//        throw new PCommandExecutionException("Request id not supported by blob cluster creation.");
//      }
      throw new UnsupportedOperationException();
    } else {
      if (requestedId == -1) {
//        finalId = db.getSchema().createBucket(name.getStringValue());
        throw new UnsupportedOperationException();
      } else {
//        result.setProperty("requestedId", requestedId);
//        finalId = db.getSchema().createBucket(name.getStringValue(), requestedId, null);
        throw new UnsupportedOperationException();
      }
    }
//    result.setProperty("finalId", finalId);
//
//    OInternalResultSet rs = new OInternalResultSet();
//    rs.add(result);
//    return rs;
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("CREATE ");
    if (blob) {
      builder.append("BLOB ");
    }
    builder.append("CLUSTER ");
    name.toString(params, builder);
    if (ifNotExists) {
      builder.append(" IF NOT EXISTS");
    }
    if (id != null) {
      builder.append(" ID ");
      id.toString(params, builder);
    }
  }

  @Override
  public CreateClusterStatement copy() {
    CreateClusterStatement result = new CreateClusterStatement(-1);
    result.name = name == null ? null : name.copy();
    result.ifNotExists = this.ifNotExists;
    result.id = id == null ? null : id.copy();
    result.blob = blob;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    CreateClusterStatement that = (CreateClusterStatement) o;

    if (ifNotExists != that.ifNotExists)
      return false;
    if (blob != that.blob)
      return false;
    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;
    return id != null ? id.equals(that.id) : that.id == null;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (ifNotExists ? 1 : 0);
    result = 31 * result + (id != null ? id.hashCode() : 0);
    result = 31 * result + (blob ? 1 : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=6011a26678f2175aa456a0a6c094cb13 (do not edit this line) */