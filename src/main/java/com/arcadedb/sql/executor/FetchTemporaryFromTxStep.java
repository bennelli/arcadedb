package com.arcadedb.sql.executor;

import com.arcadedb.database.PDocument;
import com.arcadedb.exception.PCommandExecutionException;
import com.arcadedb.exception.PTimeoutException;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

/**
 * Created by luigidellaquila on 12/01/17.
 * <p>
 * Fetches temporary records (cluster id -1) from current transaction
 */
public class FetchTemporaryFromTxStep extends AbstractExecutionStep {

  private String className;

  //runtime

  private Iterator<PDocument> txEntries;
  private Object              order;

  private long cost = 0;

  public FetchTemporaryFromTxStep(OCommandContext ctx, String className, boolean profilingEnabled) {
    super(ctx, profilingEnabled);
    this.className = className;
  }

  @Override
  public OResultSet syncPull(OCommandContext ctx, int nRecords) throws PTimeoutException {
    getPrev().ifPresent(x -> x.syncPull(ctx, nRecords));
    init();

    return new OResultSet() {

      int currentElement = 0;

      @Override
      public boolean hasNext() {
        if (txEntries == null) {
          return false;
        }
        if (currentElement >= nRecords) {
          return false;
        }
        return txEntries.hasNext();
      }

      @Override
      public OResult next() {
        long begin = profilingEnabled ? System.nanoTime() : 0;
        try {
          if (txEntries == null) {
            throw new IllegalStateException();
          }
          if (currentElement >= nRecords) {
            throw new IllegalStateException();
          }
          if (!txEntries.hasNext()) {
            throw new IllegalStateException();
          }
          PDocument record = txEntries.next();

          currentElement++;
          OResultInternal result = new OResultInternal();
          result.setElement(record);
          ctx.setVariable("$current", result);
          return result;
        } finally {
          if (profilingEnabled) {
            cost += (System.nanoTime() - begin);
          }
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
    long begin = profilingEnabled ? System.nanoTime() : 0;
    try {
      if (this.txEntries == null) {
//        Iterable<? extends ORecordOperation> iterable = ctx.getDatabase().getTransaction().getRecordOperations();
//
//        List<ORecord> records = new ArrayList<>();
//        if (iterable != null) {
//          for (ORecordOperation op : iterable) {
//            ORecord record = op.getRecord();
//            if (matchesClass(record, className) && !hasCluster(record))
//              records.add(record);
//          }
//        }
//        if (order == FetchFromClusterExecutionStep.ORDER_ASC) {
//          Collections.sort(records, new Comparator<ORecord>() {
//            @Override
//            public int compare(ORecord o1, ORecord o2) {
//              long p1 = o1.getIdentity().getClusterPosition();
//              long p2 = o2.getIdentity().getClusterPosition();
//              if (p1 == p2) {
//                return 0;
//              } else if (p1 > p2) {
//                return 1;
//              } else {
//                return -1;
//              }
//            }
//          });
//        } else {
//          Collections.sort(records, new Comparator<ORecord>() {
//            @Override
//            public int compare(ORecord o1, ORecord o2) {
//              long p1 = o1.getIdentity().getClusterPosition();
//              long p2 = o2.getIdentity().getClusterPosition();
//              if (p1 == p2) {
//                return 0;
//              } else if (p1 > p2) {
//                return -1;
//              } else {
//                return 1;
//              }
//            }
//          });
//        }
//        this.txEntries = records.iterator();
        this.txEntries = Collections.EMPTY_LIST.iterator();
      }
    } finally {
      if (profilingEnabled) {
        cost += (System.nanoTime() - begin);
      }
    }
  }

//  private boolean hasCluster(PRecord record) {
//    PRID rid = record.getIdentity();
//    if (rid == null) {
//      return false;
//    }
//    if (rid.getBucketId() < 0) {
//      return false;
//    }
//    return true;
//  }
//
//  private boolean matchesClass(PRecord record, String className) {
//    ORecord doc = record.getRecord();
//    if (!(doc instanceof ODocument)) {
//      return false;
//    }
//
//    OClass schema = ((ODocument) doc).getSchemaClass();
//    if (schema == null)
//      return className == null;
//    else if (schema.getName().equals(className)) {
//      return true;
//    } else if (schema.isSubClassOf(className)) {
//      return true;
//    }
//    return false;
//  }

  public void setOrder(Object order) {
    this.order = order;
  }

  @Override
  public String prettyPrint(int depth, int indent) {
    String spaces = OExecutionStepInternal.getIndent(depth, indent);
    StringBuilder result = new StringBuilder();
    result.append(spaces);
    result.append("+ FETCH NEW RECORDS FROM CURRENT TRANSACTION SCOPE (if any)");
    if (profilingEnabled) {
      result.append(" (" + getCostFormatted() + ")");
    }
    return result.toString();
  }

  @Override
  public OResult serialize() {
    OResultInternal result = OExecutionStepInternal.basicSerialize(this);
    result.setProperty("className", className);
    return result;
  }

  @Override
  public void deserialize(OResult fromResult) {
    try {
      OExecutionStepInternal.basicDeserialize(fromResult, this);
      className = fromResult.getProperty("className");
    } catch (Exception e) {
      throw new PCommandExecutionException(e);
    }
  }

  @Override
  public boolean canBeCached() {
    return true;
  }

  @Override
  public OExecutionStep copy(OCommandContext ctx) {
    FetchTemporaryFromTxStep result = new FetchTemporaryFromTxStep(ctx, this.className, profilingEnabled);
    return result;
  }
}