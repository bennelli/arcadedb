/* Generated By:JJTree: Do not edit this line. OContainsCondition.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.database.PIdentifiable;
import com.arcadedb.sql.executor.OCommandContext;
import com.arcadedb.sql.executor.OMultiValue;
import com.arcadedb.sql.executor.OQueryOperatorEquals;
import com.arcadedb.sql.executor.OResult;

import java.util.*;

public class ContainsCondition extends BooleanExpression {

  protected Expression        left;
  protected Expression        right;
  protected BooleanExpression condition;

  public ContainsCondition(int id) {
    super(id);
  }

  public ContainsCondition(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public boolean execute(Object left, Object right) {
    if (left instanceof Collection) {
      if (right instanceof Collection) {
        if (((Collection) left).containsAll((Collection) right)) {
          return true;
        }

        if (((Collection) right).size() == 1) {
          Object item = ((Collection) right).iterator().next();
          if (item instanceof OResult && ((OResult) item).getPropertyNames().size() == 1) {
            Object propValue = ((OResult) item).getProperty(((OResult) item).getPropertyNames().iterator().next());
            return ((Collection) left).contains(propValue);
          }
        }
        return false;
      }
      if (right instanceof Iterable) {
        right = ((Iterable) right).iterator();
      }
      if (right instanceof Iterator) {
        Iterator iterator = (Iterator) right;
        while (iterator.hasNext()) {
          Object next = iterator.next();
          if (!((Collection) left).contains(next)) {
            return false;
          }
        }
      }
      for (Object o : (Collection) left) {
        if (OQueryOperatorEquals.equals(o, right))
          return true;
      }
      return false;
    }
    if (left instanceof Iterable) {
      left = ((Iterable) left).iterator();
    }
    if (left instanceof Iterator) {
      if (!(right instanceof Iterable)) {
        right = Collections.singleton(right);
      }
      right = ((Iterable) right).iterator();

      Iterator leftIterator = (Iterator) left;
      Iterator rightIterator = (Iterator) right;
      while (rightIterator.hasNext()) {
        Object leftItem = rightIterator.next();
        boolean found = false;
        while (leftIterator.hasNext()) {
          Object rightItem = leftIterator.next();
          if (leftItem != null && leftItem.equals(rightItem)) {
            found = true;
            break;
          }
        }
        if (!found) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean evaluate(PIdentifiable currentRecord, OCommandContext ctx) {
    Object leftValue = left.execute(currentRecord, ctx);
    if (right != null) {
      Object rightValue = right.execute(currentRecord, ctx);
      return execute(leftValue, rightValue);
    } else {
      if (!OMultiValue.isMultiValue(leftValue)) {
        return false;
      }
      Iterator<Object> iter = OMultiValue.getMultiValueIterator(leftValue);
      while (iter.hasNext()) {
        Object item = iter.next();
        if (item instanceof PIdentifiable && condition.evaluate((PIdentifiable) item, ctx)) {
          return true;
        } else if (item instanceof OResult && condition.evaluate((OResult) item, ctx)) {
          return true;
        }
      }
      return false;
    }
  }

  @Override
  public boolean evaluate(OResult currentRecord, OCommandContext ctx) {
    Object leftValue = left.execute(currentRecord, ctx);
    if (right != null) {
      Object rightValue = right.execute(currentRecord, ctx);
      return execute(leftValue, rightValue);
    } else {
      if (!OMultiValue.isMultiValue(leftValue)) {
        return false;
      }
      Iterator<Object> iter = OMultiValue.getMultiValueIterator(leftValue);
      while (iter.hasNext()) {
        Object item = iter.next();
        if (item instanceof PIdentifiable && condition.evaluate((PIdentifiable) item, ctx)) {
          return true;
        } else if (item instanceof OResult && condition.evaluate((OResult) item, ctx)) {
          return true;
        }
      }
      return false;
    }
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    left.toString(params, builder);
    builder.append(" CONTAINS ");
    if (right != null) {
      right.toString(params, builder);
    } else if (condition != null) {
      builder.append("(");
      condition.toString(params, builder);
      builder.append(")");
    }
  }

  @Override
  public boolean supportsBasicCalculation() {
    if (!left.supportsBasicCalculation()) {
      return false;
    }
    if (!right.supportsBasicCalculation()) {
      return false;
    }
    if (!condition.supportsBasicCalculation()) {
      return false;
    }

    return true;
  }

  @Override
  protected int getNumberOfExternalCalculations() {
    int total = 0;
    if (condition != null) {
      total += condition.getNumberOfExternalCalculations();
    }
    if (!left.supportsBasicCalculation()) {
      total++;
    }
    if (right != null && !right.supportsBasicCalculation()) {
      total++;
    }
    return total;
  }

  @Override
  protected List<Object> getExternalCalculationConditions() {
    List<Object> result = new ArrayList<Object>();

    if (condition != null) {
      result.addAll(condition.getExternalCalculationConditions());
    }
    if (!left.supportsBasicCalculation()) {
      result.add(left);
    }
    if (right != null && !right.supportsBasicCalculation()) {
      result.add(right);
    }
    return result;
  }

  @Override
  public boolean needsAliases(Set<String> aliases) {
    if (left != null && left.needsAliases(aliases)) {
      return true;
    }
    if (right != null && right.needsAliases(aliases)) {
      return true;
    }
    if (condition != null && condition.needsAliases(aliases)) {
      return true;
    }
    return false;
  }

  @Override
  public ContainsCondition copy() {
    ContainsCondition result = new ContainsCondition(-1);
    result.left = left == null ? null : left.copy();
    result.right = right == null ? null : right.copy();
    result.condition = condition == null ? null : condition.copy();
    return result;

  }

  @Override
  public void extractSubQueries(SubQueryCollector collector) {
    if (left != null) {
      left.extractSubQueries(collector);
    }
    if (right != null) {
      right.extractSubQueries(collector);
    }
    if (condition != null) {
      condition.extractSubQueries(collector);
    }
  }

  @Override
  public boolean refersToParent() {
    if (left != null && left.refersToParent()) {
      return true;
    }
    if (right != null && right.refersToParent()) {
      return true;
    }
    if (condition != null && condition.refersToParent()) {
      return true;
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    ContainsCondition that = (ContainsCondition) o;

    if (left != null ? !left.equals(that.left) : that.left != null)
      return false;
    if (right != null ? !right.equals(that.right) : that.right != null)
      return false;
    if (condition != null ? !condition.equals(that.condition) : that.condition != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = left != null ? left.hashCode() : 0;
    result = 31 * result + (right != null ? right.hashCode() : 0);
    result = 31 * result + (condition != null ? condition.hashCode() : 0);
    return result;
  }

  @Override
  public List<String> getMatchPatternInvolvedAliases() {
    List<String> leftX = left == null ? null : left.getMatchPatternInvolvedAliases();
    List<String> rightX = right == null ? null : right.getMatchPatternInvolvedAliases();
    List<String> conditionX = condition == null ? null : condition.getMatchPatternInvolvedAliases();

    List<String> result = new ArrayList<String>();
    if (leftX != null) {
      result.addAll(leftX);
    }
    if (rightX != null) {
      result.addAll(rightX);
    }
    if (conditionX != null) {
      result.addAll(conditionX);
    }

    return result.size() == 0 ? null : result;
  }

  @Override
  public boolean isCacheable() {
    if (left != null && !left.isCacheable()) {
      return false;
    }
    if (right != null && !right.isCacheable()) {
      return false;
    }
    if (condition != null && !condition.isCacheable()) {
      return false;
    }
    return true;
  }

}
/* JavaCC - OriginalChecksum=bad1118296ea74860e88d66bfe9fa222 (do not edit this line) */