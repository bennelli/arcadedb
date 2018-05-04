package com.arcadedb.sql.executor;

import com.arcadedb.database.PModifiableDocument;
import com.arcadedb.database.PRID;
import com.arcadedb.exception.PCommandExecutionException;
import com.arcadedb.exception.PTimeoutException;
import com.arcadedb.graph.PEdge;
import com.arcadedb.graph.PVertex;
import com.arcadedb.sql.parser.Batch;
import com.arcadedb.sql.parser.Identifier;

import java.util.*;

/**
 * Created by luigidellaquila on 28/11/16.
 */
public class CreateEdgesStep extends AbstractExecutionStep {

  private final Identifier targetClass;
  private final Identifier targetCluster;
  private final Identifier fromAlias;
  private final Identifier toAlias;
  private final Number     wait;
  private final Number     retry;
  private final Batch      batch;

  //operation stuff
  Iterator fromIter;
  Iterator toIterator;
  PVertex  currentFrom;
  List toList = new ArrayList<>();
  private boolean inited = false;

  private long cost = 0;

  public CreateEdgesStep(Identifier targetClass, Identifier targetClusterName, Identifier fromAlias, Identifier toAlias,
      Number wait, Number retry, Batch batch, OCommandContext ctx, boolean profilingEnabled) {
    super(ctx, profilingEnabled);
    this.targetClass = targetClass;
    this.targetCluster = targetClusterName;
    this.fromAlias = fromAlias;
    this.toAlias = toAlias;
    this.wait = wait;
    this.retry = retry;
    this.batch = batch;

  }

  @Override
  public OResultSet syncPull(OCommandContext ctx, int nRecords) throws PTimeoutException {
    getPrev().ifPresent(x -> x.syncPull(ctx, nRecords));
    init();
    return new OResultSet() {
      int currentBatch = 0;

      @Override
      public boolean hasNext() {
        return (currentBatch < nRecords && (toIterator.hasNext() || (toList.size() > 0 && fromIter.hasNext())));
      }

      @Override
      public OResult next() {
        if (!toIterator.hasNext()) {
          toIterator = toList.iterator();
          if (!fromIter.hasNext()) {
            throw new IllegalStateException();
          }
          currentFrom = fromIter.hasNext() ? asVertex(fromIter.next()) : null;
        }
        if (currentBatch < nRecords && (toIterator.hasNext() || (toList.size() > 0 && fromIter.hasNext()))) {

          if (currentFrom == null) {
            throw new PCommandExecutionException("Invalid FROM vertex for edge");
          }

          Object obj = toIterator.next();
          long begin = profilingEnabled ? System.nanoTime() : 0;
          try {
            PVertex currentTo = asVertex(obj);
            if (currentTo == null) {
              throw new PCommandExecutionException("Invalid TO vertex for edge");
            }

            PEdge edge = currentFrom.newEdge(targetClass.getStringValue(), currentTo, true);

            if (!(edge instanceof PModifiableDocument)) {
              throw new UnsupportedOperationException("How to make an unmodifiable edge modifiable?");
            }
            OUpdatableResult result = new OUpdatableResult((PModifiableDocument) edge);
            result.setElement(edge);
            currentBatch++;
            return result;
          } finally {
            if (profilingEnabled) {
              cost += (System.nanoTime() - begin);
            }
          }
        } else {
          throw new IllegalStateException();
        }
      }

      @Override
      public void close() {

      }

      @Override
      public Optional<OExecutionPlan> getExecutionPlan() {
        return null;
      }

      @Override
      public Map<String, Long> getQueryStats() {
        return null;
      }
    };
  }

  private void init() {
    synchronized (this) {
      if (this.inited) {
        return;
      }
      inited = true;
    }
    Object fromValues = ctx.getVariable(fromAlias.getStringValue());
    if (fromValues instanceof Iterable) {
      fromValues = ((Iterable) fromValues).iterator();
    } else if (!(fromValues instanceof Iterator)) {
      fromValues = Collections.singleton(fromValues).iterator();
    }

    Object toValues = ctx.getVariable(toAlias.getStringValue());
    if (toValues instanceof Iterable) {
      toValues = ((Iterable) toValues).iterator();
    } else if (!(toValues instanceof Iterator)) {
      toValues = Collections.singleton(toValues).iterator();
    }

    fromIter = (Iterator) fromValues;
    if (fromIter instanceof OResultSet) {
      try {
        ((OResultSet) fromIter).reset();
      } catch (Exception ignore) {
      }
    }

    Iterator toIter = (Iterator) toValues;

    while (toIter != null && toIter.hasNext()) {
      toList.add(toIter.next());
    }

    toIterator = toList.iterator();
    if (toIter instanceof OResultSet) {
      try {
        ((OResultSet) toIter).reset();
      } catch (Exception ignore) {
      }
    }

    currentFrom = fromIter != null && fromIter.hasNext() ? asVertex(fromIter.next()) : null;

  }

  private PVertex asVertex(Object currentFrom) {
    if (currentFrom instanceof PRID) {
      currentFrom = ((PRID) currentFrom).getRecord();
    }
    if (currentFrom instanceof OResult && ((OResult) currentFrom).isVertex()) {
      return (PVertex) ((OResult) currentFrom).getElement().get();
    }
    if (currentFrom instanceof PVertex) {
      return (PVertex) currentFrom;
    }
    return null;
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    String spaces = OExecutionStepInternal.getIndent(depth, indent);
    String result = spaces + "+ FOR EACH x in " + fromAlias + "\n";
    result += spaces + "    FOR EACH y in " + toAlias + "\n";
    result += spaces + "       CREATE EDGE " + targetClass + " FROM x TO y";
    if (profilingEnabled) {
      result += " (" + getCostFormatted() + ")";
    }
    if (targetCluster != null) {
      result += "\n" + spaces + "       (target cluster " + targetCluster + ")";
    }
    return result;
  }

  @Override
  public long getCost() {
    return cost;
  }
}

