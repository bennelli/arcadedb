/* Generated By:JJTree: Do not edit this line. OArraySingleValuesSelector.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.orientechnologies.common.collection.OMultiValue;
import com.orientechnologies.orient.core.command.OCommandContext;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.exception.OCommandExecutionException;
import com.orientechnologies.orient.core.record.OElement;
import com.orientechnologies.orient.core.sql.executor.OResult;
import com.orientechnologies.orient.core.sql.executor.OResultInternal;

import java.util.*;
import java.util.stream.Collectors;

public class OArraySingleValuesSelector extends SimpleNode {

  protected List<OArraySelector> items = new ArrayList<OArraySelector>();

  public OArraySingleValuesSelector(int id) {
    super(id);
  }

  public OArraySingleValuesSelector(OrientSql p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(OrientSqlVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    boolean first = true;
    for (OArraySelector item : items) {
      if (!first) {
        builder.append(",");
      }
      item.toString(params, builder);
      first = false;
    }
  }

  public Object execute(OIdentifiable iCurrentRecord, Object iResult, OCommandContext ctx) {
    List<Object> result = new ArrayList<Object>();
    for (OArraySelector item : items) {
      Integer index = item.getValue(iCurrentRecord, iResult, ctx);
      if (this.items.size() == 1) {
        return OMultiValue.getValue(iResult, index);
      }
      result.add(OMultiValue.getValue(iResult, index));
    }
    return result;
  }

  public Object execute(OResult iCurrentRecord, Object iResult, OCommandContext ctx) {
    List<Object> result = new ArrayList<Object>();
    for (OArraySelector item : items) {
      Object index = item.getValue(iCurrentRecord, iResult, ctx);
      if (index == null) {
        return null;
      }

      if (index instanceof Integer) {
        result.add(OMultiValue.getValue(iResult, ((Integer) index).intValue()));
      } else {
        if (iResult instanceof Map) {
          result.add(((Map) iResult).get(index));
        } else if (iResult instanceof OElement && index instanceof String) {
          result.add(((OElement) iResult).getProperty((String) index));
        } else {
          result.add(null);
        }
      }
      if (this.items.size() == 1) {
        return result.get(0);
      }
    }
    return result;
  }

  public boolean needsAliases(Set<String> aliases) {
    for (OArraySelector item : items) {
      if (item.needsAliases(aliases)) {
        return true;
      }
    }
    return false;
  }

  public OArraySingleValuesSelector copy() {
    OArraySingleValuesSelector result = new OArraySingleValuesSelector(-1);
    result.items = items.stream().map(x -> x.copy()).collect(Collectors.toList());
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    OArraySingleValuesSelector that = (OArraySingleValuesSelector) o;

    if (items != null ? !items.equals(that.items) : that.items != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return items != null ? items.hashCode() : 0;
  }

  public void extractSubQueries(SubQueryCollector collector) {
    if (items != null) {
      for (OArraySelector item : items) {
        item.extractSubQueries(collector);
      }
    }
  }

  public boolean refersToParent() {
    if (items != null) {
      for (OArraySelector item : items) {
        if (item.refersToParent()) {
          return true;
        }
      }
    }
    return false;
  }

  public void setValue(OResult currentRecord, Object target, Object value, OCommandContext ctx) {
    if (items != null) {
      for (OArraySelector item : items) {
        item.setValue(currentRecord, target, value, ctx);
      }
    }
  }

  public void applyRemove(Object currentValue, OResultInternal originalRecord, OCommandContext ctx) {
    if (currentValue == null) {
      return;
    }
    List values = this.items.stream().map(x -> x.getValue(originalRecord, null, ctx)).collect(Collectors.toList());
    if (currentValue instanceof List) {
      List<Object> list = (List) currentValue;
      Collections.sort(values, this::compareKeysForRemoval);
      for (Object val : values) {
        if (val instanceof Integer) {
          list.remove((int) (Integer) val);
        } else {
          list.remove(val);
        }
      }
    } else if (currentValue instanceof Set) {
      Set set = (Set) currentValue;
      Iterator iterator = set.iterator();
      int count = 0;
      while (iterator.hasNext()) {
        Object item = iterator.next();
        if (values.contains(count) || values.contains(item)) {
          iterator.remove();
        }
      }
    } else {
      throw new OCommandExecutionException(
          "Trying to remove elements from " + currentValue + " (" + currentValue.getClass().getSimpleName() + ")");
    }

  }

  private int compareKeysForRemoval(Object o1, Object o2) {
    if (o1 instanceof Integer) {
      if (o2 instanceof Integer) {
        return (int) o2 - (int) o1;
      } else {
        return -1;
      }
    } else if (o2 instanceof Integer) {
      return 1;
    } else {
      return 0;
    }
  }

  public OResult serialize() {
    OResultInternal result = new OResultInternal();
    if (items != null) {
      result.setProperty("items", items.stream().map(x -> x.serialize()).collect(Collectors.toList()));
    }
    return result;
  }

  public void deserialize(OResult fromResult) {

    if (fromResult.getProperty("items") != null) {
      List<OResult> ser = fromResult.getProperty("items");
      items = new ArrayList<>();
      for (OResult r : ser) {
        OArraySelector exp = new OArraySelector(-1);
        exp.deserialize(r);
        items.add(exp);
      }
    }
  }
}
/* JavaCC - OriginalChecksum=991998c77a4831184b6dca572513fd8d (do not edit this line) */
