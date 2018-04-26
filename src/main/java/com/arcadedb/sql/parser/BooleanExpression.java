package com.arcadedb.sql.parser;

import com.arcadedb.database.PDatabase;
import com.arcadedb.database.PIdentifiable;
import com.arcadedb.exception.PCommandExecutionException;
import com.arcadedb.schema.PDocumentType;
import com.arcadedb.sql.executor.OCommandContext;
import com.arcadedb.sql.executor.OResult;
import com.arcadedb.sql.executor.OResultInternal;

import java.util.*;

/**
 * Created by luigidellaquila on 07/11/14.
 */
public abstract class BooleanExpression extends SimpleNode {

  public static final BooleanExpression TRUE = new BooleanExpression(0) {
    @Override
    public boolean evaluate(PIdentifiable currentRecord, OCommandContext ctx) {
      return true;
    }

    @Override
    public boolean evaluate(OResult currentRecord, OCommandContext ctx) {
      return true;
    }

    @Override
    protected boolean supportsBasicCalculation() {
      return true;
    }

    @Override
    protected int getNumberOfExternalCalculations() {
      return 0;
    }

    @Override
    protected List<Object> getExternalCalculationConditions() {
      return Collections.EMPTY_LIST;
    }

    @Override
    public boolean needsAliases(Set<String> aliases) {
      return false;
    }

    @Override
    public BooleanExpression copy() {
      return TRUE;

    }

    @Override
    public List<String> getMatchPatternInvolvedAliases() {
      return null;
    }

    @Override
    public void translateLuceneOperator() {
    }

    @Override
    public boolean isCacheable() {
      return true;
    }

    @Override
    public String toString() {
      return "true";
    }

    public void toString(Map<Object, Object> params, StringBuilder builder) {
      builder.append("true");
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public void extractSubQueries(SubQueryCollector collector) {

    }

    @Override
    public boolean refersToParent() {
      return false;
    }

  };

  public static final BooleanExpression FALSE = new BooleanExpression(0) {
    @Override
    public boolean evaluate(PIdentifiable currentRecord, OCommandContext ctx) {
      return false;
    }

    @Override
    public boolean evaluate(OResult currentRecord, OCommandContext ctx) {
      return false;
    }

    @Override
    protected boolean supportsBasicCalculation() {
      return true;
    }

    @Override
    protected int getNumberOfExternalCalculations() {
      return 0;
    }

    @Override
    protected List<Object> getExternalCalculationConditions() {
      return Collections.EMPTY_LIST;
    }

    @Override
    public boolean needsAliases(Set<String> aliases) {
      return false;
    }

    @Override
    public BooleanExpression copy() {
      return FALSE;
    }

    @Override
    public List<String> getMatchPatternInvolvedAliases() {
      return null;
    }

    @Override
    public void translateLuceneOperator() {

    }

    @Override
    public boolean isCacheable() {
      return true;
    }

    @Override
    public String toString() {
      return "false";
    }

    public void toString(Map<Object, Object> params, StringBuilder builder) {
      builder.append("false");
    }

    @Override
    public boolean isEmpty() {
      return false;
    }

    @Override
    public void extractSubQueries(SubQueryCollector collector) {

    }

    @Override
    public boolean refersToParent() {
      return false;
    }

  };

  public BooleanExpression(int id) {
    super(id);
  }

  public BooleanExpression(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public abstract boolean evaluate(PIdentifiable currentRecord, OCommandContext ctx);

  public abstract boolean evaluate(OResult currentRecord, OCommandContext ctx);

  /**
   * @return true if this expression can be calculated in plain Java, false otherwise (eg. LUCENE operator)
   */
  protected abstract boolean supportsBasicCalculation();

  /**
   * @return the number of sub-expressions that have to be calculated using an external engine (eg. LUCENE)
   */
  protected abstract int getNumberOfExternalCalculations();

  /**
   * @return the sub-expressions that have to be calculated using an external engine (eg. LUCENE)
   */
  protected abstract List<Object> getExternalCalculationConditions();

  public List<BinaryCondition> getIndexedFunctionConditions(PDocumentType iSchemaClass, PDatabase database) {
    return null;
  }

  public List<AndBlock> flatten() {

    return Collections.singletonList(encapsulateInAndBlock(this));
  }

  protected AndBlock encapsulateInAndBlock(BooleanExpression item) {
    if (item instanceof AndBlock) {
      return (AndBlock) item;
    }
    AndBlock result = new AndBlock(-1);
    result.subBlocks.add(item);
    return result;
  }

  public abstract boolean needsAliases(Set<String> aliases);

  public abstract BooleanExpression copy();

  public boolean isEmpty() {
    return false;
  }

  public abstract void extractSubQueries(SubQueryCollector collector);

  public abstract boolean refersToParent();

  /**
   * returns the equivalent of current condition as an UPDATE expression with the same syntax, if possible.
   * <p>
   * Eg. name = 3 can be considered a condition or an assignment. This method transforms the condition in an assignment.
   * This is used mainly for UPSERT operations.
   *
   * @return the equivalent of current condition as an UPDATE expression with the same syntax, if possible.
   */
  public Optional<UpdateItem> transformToUpdateItem() {
    return Optional.empty();
  }

  public abstract List<String> getMatchPatternInvolvedAliases();

  public void translateLuceneOperator() {

  }

  public static BooleanExpression deserializeFromOResult(OResult doc) {
    try {
      BooleanExpression result = (BooleanExpression) Class.forName(doc.getProperty("__class")).getConstructor(Integer.class)
          .newInstance(-1);
      result.deserialize(doc);
    } catch (Exception e) {
      throw new PCommandExecutionException("", e);
    }
    return null;
  }

  public OResult serialize() {
    OResultInternal result = new OResultInternal();
    result.setProperty("__class", getClass().getName());
    return result;
  }

  public void deserialize(OResult fromResult) {
    throw new UnsupportedOperationException();
  }

  public abstract boolean isCacheable();
}