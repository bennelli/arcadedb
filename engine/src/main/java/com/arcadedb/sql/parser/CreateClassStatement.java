/* Generated By:JJTree: Do not edit this line. OCreateClassStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.exception.PCommandExecutionException;
import com.arcadedb.schema.PDocumentType;
import com.arcadedb.schema.PSchema;
import com.arcadedb.sql.executor.OCommandContext;
import com.arcadedb.sql.executor.OInternalResultSet;
import com.arcadedb.sql.executor.OResultInternal;
import com.arcadedb.sql.executor.OResultSet;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateClassStatement extends ODDLStatement {
  /**
   * Class name
   */
  public Identifier name;

  public boolean ifNotExists;

  /**
   * Direct superclasses for this class
   */
  protected List<Identifier> superclasses;

  /**
   * Cluster IDs for this class
   */
  protected List<PInteger> buckets;

  /**
   * Total number clusters for this class
   */
  protected PInteger totalClusterNo;

  protected boolean abstractClass = false;

  public CreateClassStatement(int id) {
    super(id);
  }

  public CreateClassStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override
  public OResultSet executeDDL(OCommandContext ctx) {

    PSchema schema = ctx.getDatabase().getSchema();
    if (schema.existsType(name.getStringValue())) {
      if (ifNotExists) {
        return new OInternalResultSet();
      } else {
        throw new PCommandExecutionException("Class " + name + " already exists");
      }
    }
    checkSuperTypes(schema, ctx);

    OResultInternal result = new OResultInternal();
    result.setProperty("operation", "create class");
    result.setProperty("className", name.getStringValue());

    PDocumentType type = null;
    PDocumentType[] superclasses = getSuperTypes(schema);

    if (totalClusterNo != null) {
      type = schema.createDocumentType(name.getStringValue(), totalClusterNo.getValue().intValue());
    } else {
      type = schema.createDocumentType(name.getStringValue());
    }

    for (PDocumentType c : superclasses)
      type.addParent(c);

    OInternalResultSet rs = new OInternalResultSet();
    rs.add(result);
    return rs;
  }

  private PDocumentType[] getSuperTypes(PSchema schema) {
    if (superclasses == null) {
      return new PDocumentType[] {};
    }
    return superclasses.stream().map(x -> schema.getType(x.getStringValue())).filter(x -> x != null).collect(Collectors.toList())
        .toArray(new PDocumentType[] {});
  }

  private void checkSuperTypes(PSchema schema, OCommandContext ctx) {
    if (superclasses != null) {
      for (Identifier superType : superclasses) {
        if (!schema.existsType(superType.value)) {
          throw new PCommandExecutionException("Supertype " + superType + " not found");
        }
      }
    }
  }

  @Override
  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("CREATE CLASS ");
    name.toString(params, builder);
    if (ifNotExists) {
      builder.append(" IF NOT EXISTS");
    }
    if (superclasses != null && superclasses.size() > 0) {
      builder.append(" EXTENDS ");
      boolean first = true;
      for (Identifier sup : superclasses) {
        if (!first) {
          builder.append(", ");
        }
        sup.toString(params, builder);
        first = false;
      }
    }
    if (buckets != null && buckets.size() > 0) {
      builder.append(" CLUSTER ");
      boolean first = true;
      for (PInteger cluster : buckets) {
        if (!first) {
          builder.append(",");
        }
        cluster.toString(params, builder);
        first = false;
      }
    }
    if (totalClusterNo != null) {
      builder.append(" CLUSTERS ");
      totalClusterNo.toString(params, builder);
    }
    if (abstractClass) {
      builder.append(" ABSTRACT");
    }
  }

  @Override
  public CreateClassStatement copy() {
    CreateClassStatement result = new CreateClassStatement(-1);
    result.name = name == null ? null : name.copy();
    result.superclasses = superclasses == null ? null : superclasses.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.buckets = buckets == null ? null : buckets.stream().map(x -> x.copy()).collect(Collectors.toList());
    result.totalClusterNo = totalClusterNo == null ? null : totalClusterNo.copy();
    result.abstractClass = abstractClass;
    result.ifNotExists = ifNotExists;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    CreateClassStatement that = (CreateClassStatement) o;

    if (abstractClass != that.abstractClass)
      return false;
    if (name != null ? !name.equals(that.name) : that.name != null)
      return false;
    if (superclasses != null ? !superclasses.equals(that.superclasses) : that.superclasses != null)
      return false;
    if (buckets != null ? !buckets.equals(that.buckets) : that.buckets != null)
      return false;
    if (totalClusterNo != null ? !totalClusterNo.equals(that.totalClusterNo) : that.totalClusterNo != null)
      return false;
    if (ifNotExists != that.ifNotExists) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (superclasses != null ? superclasses.hashCode() : 0);
    result = 31 * result + (buckets != null ? buckets.hashCode() : 0);
    result = 31 * result + (totalClusterNo != null ? totalClusterNo.hashCode() : 0);
    result = 31 * result + (abstractClass ? 1 : 0);
    return result;
  }

  public List<Identifier> getSuperclasses() {
    return superclasses;
  }
}
/* JavaCC - OriginalChecksum=4043013624f55fdf0ea8fee6d4f211b0 (do not edit this line) */