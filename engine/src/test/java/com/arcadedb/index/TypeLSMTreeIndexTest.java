/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.index;

import com.arcadedb.BaseTest;
import com.arcadedb.GlobalConfiguration;
import com.arcadedb.database.*;
import com.arcadedb.exception.DuplicatedKeyException;
import com.arcadedb.exception.NeedRetryException;
import com.arcadedb.log.LogManager;
import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.SchemaImpl;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

public class TypeLSMTreeIndexTest extends BaseTest {
  private static final int    TOT       = 100000;
  private static final String TYPE_NAME = "V";
  private static final int    PAGE_SIZE = 20000;

  @Test
  public void testGet() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();

        for (int i = 0; i < TOT; ++i) {
          final List<Integer> results = new ArrayList<>();
          for (Index index : indexes) {
            final IndexCursor value = index.get(new Object[] { i });
            if (value.hasNext())
              results.add((Integer) ((Document) value.next().getRecord()).get("id"));
          }

          total++;
          Assertions.assertEquals(1, results.size());
          Assertions.assertEquals(i, (int) results.get(0));
        }

        Assertions.assertEquals(TOT, total);
      }
    });
  }

  @Test
  public void testGetAsRange() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {

        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();
        for (int i = 0; i < TOT; ++i) {
          int total = 0;

          for (Index index : indexes) {
            Assertions.assertNotNull(index);

            final IndexCursor iterator;
            try {
              iterator = ((RangeIndex) index).range(new Object[] { i }, true, new Object[] { i }, true);
              Assertions.assertNotNull(iterator);

              while (iterator.hasNext()) {
                Identifiable value = iterator.next();

                Assertions.assertNotNull(value);

                int fieldValue = (int) ((Document) value.getRecord()).get("id");
                Assertions.assertEquals(i, fieldValue);

                Assertions.assertNotNull(iterator.getKeys());
                Assertions.assertEquals(1, iterator.getKeys().length);

                total++;
              }
            } catch (Exception e) {
              Assertions.fail(e);
            }
          }

          Assertions.assertEquals(1, total, "Get item with id=" + i);
        }
      }
    });
  }

  @Test
  public void testRangeFromHead() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {

        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();
        for (int i = 0; i < TOT - 1; ++i) {
          int total = 0;

          for (Index index : indexes) {
            Assertions.assertNotNull(index);

            final IndexCursor iterator;
            iterator = ((RangeIndex) index).range(new Object[] { i }, true, new Object[] { i + 1 }, true);
            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Identifiable value = iterator.next();

              Assertions.assertNotNull(value);

              int fieldValue = (int) ((Document) value.getRecord()).get("id");
              Assertions.assertTrue(fieldValue >= i && fieldValue <= i + 1);

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              ++total;
            }
          }

          Assertions.assertEquals(2, total, "range " + i + "-" + (i + 1));
        }
      }
    });
  }

//  @Test
//  // TODO: fix this test
//  public void testRangeFromTail() {
//    database.transaction(new Database.TransactionScope() {
//      @Override
//      public void execute(Database database) {
//
//        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();
//        for (int i = TOT - 1; i > 0; --i) {
//          int total = 0;
//
//          for (Index index : indexes) {
//            Assertions.assertNotNull(index);
//
//            final IndexCursor iterator;
//            iterator = ((RangeIndex) index).range(new Object[] { i }, true, new Object[] { i - 1 }, true);
//            Assertions.assertNotNull(iterator);
//
//            while (iterator.hasNext()) {
//              Identifiable value = iterator.next();
//
//              Assertions.assertNotNull(value);
//
//              int fieldValue = (int) ((Document) value.getRecord()).get("id");
//              Assertions.assertTrue(fieldValue >= i - 1 && fieldValue <= i);
//
//              Assertions.assertNotNull(iterator.getKeys());
//              Assertions.assertEquals(1, iterator.getKeys().length);
//
//              ++total;
//            }
//          }
//
//          Assertions.assertEquals(2, total, "range " + i + "-" + (i - 1));
//        }
//      }
//    });
//  }

//  @Test
//  // TODO: fix this test
//  public void testRangeWithSQL() {
//    database.transaction(new Database.TransactionScope() {
//      @Override
//      public void execute(Database database) {
//        for (int i = 0; i < TOT - 1; ++i) {
//          int total = 0;
//
//          final ResultSet iterator;
//          try {
//            iterator = database.command("sql", "select from " + TYPE_NAME + " where id >= " + i + " and id <= " + (i + 1));
//            Assertions.assertNotNull(iterator);
//
//            while (iterator.hasNext()) {
//              Result value = iterator.next();
//
//              Assertions.assertNotNull(value);
//
//              int fieldValue = (int) value.getProperty("id");
//              Assertions.assertTrue(fieldValue >= i && fieldValue <= i + 1);
//
//              total++;
//            }
//          } catch (Exception e) {
//            Assertions.fail(e);
//          }
//
//          Assertions.assertEquals(2, total, "For ids >= " + i + " and <= " + (i + 1));
//        }
//      }
//    });
//  }

  @Test
  public void testScanIndexAscending() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {

        try {
          // WAIT FOR THE INDEX TO BE COMPACTED
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        int total = 0;

        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = ((RangeIndex) index).iterator(true);

//            LogManager.instance()
//                .log(this, Level.INFO, "*****************************************************************************\nCURSOR BEGIN%s", iterator.dumpStats());

            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Assertions.assertNotNull(iterator.next());

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }

//            LogManager.instance().log(this, Level.INFO, "*****************************************************************************\nCURSOR END total=%d %s", total,
//                iterator.dumpStats());

          } catch (Exception e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(TOT, total);
      }
    });
  }

  @Test
  public void testScanIndexDescending() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {

        try {
          // WAIT FOR THE INDEX TO BE COMPACTED
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        int total = 0;

        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = ((RangeIndex) index).iterator(false);
            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Assertions.assertNotNull(iterator.next());

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              //LogManager.instance().log(this, Level.INFO, "Index %s Key %s", null, index, Arrays.toString(iterator.getKeys()));

              total++;
            }
          } catch (Exception e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(TOT, total);
      }
    });
  }

  @Test
  public void testScanIndexAscendingPartialInclusive() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = ((RangeIndex) index).iterator(true, new Object[] { 10 }, true);

            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Assertions.assertNotNull(iterator.next());

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }
          } catch (Exception e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(TOT - 10, total);
      }
    });
  }

  @Test
  public void testScanIndexAscendingPartialExclusive() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = ((RangeIndex) index).iterator(true, new Object[] { 10 }, false);

            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Assertions.assertNotNull(iterator.next());

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }
          } catch (Exception e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(TOT - 11, total);
      }
    });
  }

  @Test
  public void testScanIndexDescendingPartialInclusive() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = ((RangeIndex) index).iterator(false, new Object[] { 9 }, true);
            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Assertions.assertNotNull(iterator.next());

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }
          } catch (Exception e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(10, total);
      }
    });
  }

  @Test
  public void testScanIndexDescendingPartialExclusive() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = ((RangeIndex) index).iterator(false, new Object[] { 9 }, false);
            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Assertions.assertNotNull(iterator.next());

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }
          } catch (Exception e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(9, total);
      }
    });
  }

  @Test
  public void testScanIndexRangeInclusive2Inclusive() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = ((RangeIndex) index).range(new Object[] { 10 }, true, new Object[] { 19 }, true);
            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Identifiable value = iterator.next();

              Assertions.assertNotNull(value);

              int fieldValue = (int) ((Document) value.getRecord()).get("id");
              Assertions.assertTrue(fieldValue >= 10 && fieldValue <= 19);

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }
          } catch (Exception e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(10, total);
      }
    });
  }

  @Test
  public void testScanIndexRangeInclusive2Exclusive() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = ((RangeIndex) index).range(new Object[] { 10 }, true, new Object[] { 19 }, false);
            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Identifiable value = iterator.next();

              Assertions.assertNotNull(value);

              int fieldValue = (int) ((Document) value.getRecord()).get("id");
              Assertions.assertTrue(fieldValue >= 10 && fieldValue < 19);

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }
          } catch (Exception e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(9, total);
      }
    });
  }

  @Test
  public void testScanIndexRangeExclusive2Inclusive() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = ((RangeIndex) index).range(new Object[] { 10 }, false, new Object[] { 19 }, true);
            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Identifiable value = iterator.next();

              Assertions.assertNotNull(value);

              int fieldValue = (int) ((Document) value.getRecord()).get("id");
              Assertions.assertTrue(fieldValue > 10 && fieldValue <= 19);

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }
          } catch (Exception e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(9, total);
      }
    });
  }

  @Test
  public void testScanIndexRangeExclusive2Exclusive() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {

        int total = 0;

        final TypeIndex[] indexes = database.getSchema().getType(TYPE_NAME).getAllIndexes();
        for (Index index : indexes) {
          Assertions.assertNotNull(index);

          final IndexCursor iterator;
          try {
            iterator = ((RangeIndex) index).range(new Object[] { 10 }, false, new Object[] { 19 }, false);
            Assertions.assertNotNull(iterator);

            while (iterator.hasNext()) {
              Identifiable value = iterator.next();

              Assertions.assertNotNull(value);

              int fieldValue = (int) ((Document) value.getRecord()).get("id");
              Assertions.assertTrue(fieldValue > 10 && fieldValue < 19);

              Assertions.assertNotNull(iterator.getKeys());
              Assertions.assertEquals(1, iterator.getKeys().length);

              total++;
            }
          } catch (Exception e) {
            Assertions.fail(e);
          }
        }

        Assertions.assertEquals(8, total);
      }
    });
  }

  @Test
  public void testUnique() {
    GlobalConfiguration.INDEX_COMPACTION_MIN_PAGES_SCHEDULE.setValue(0);

    database.begin();
    final long startingWith = database.countType(TYPE_NAME, true);

    final long total = 2000;
    final int maxRetries = 100;

    final Thread[] threads = new Thread[Runtime.getRuntime().availableProcessors() * 4];

    final AtomicLong needRetryExceptions = new AtomicLong();
    final AtomicLong duplicatedExceptions = new AtomicLong();
    final AtomicLong crossThreadsInserted = new AtomicLong();

    LogManager.instance().log(this, Level.INFO, "%s Started with %d threads", null, getClass(), threads.length);

    for (int i = 0; i < threads.length; ++i) {
      threads[i] = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            int threadInserted = 0;
            for (int i = TOT; i < TOT + total; ++i) {
              boolean keyPresent = false;
              for (int retry = 0; retry < maxRetries && !keyPresent; ++retry) {

                try {
                  Thread.sleep(new Random().nextInt(10));
                } catch (InterruptedException e) {
                  e.printStackTrace();
                  Thread.currentThread().interrupt();
                  return;
                }

                database.begin();
                try {
                  final MutableDocument v = database.newDocument(TYPE_NAME);
                  v.set("id", i);
                  v.set("name", "Jay");
                  v.set("surname", "Miner");
                  v.save();

                  database.commit();

                  threadInserted++;
                  crossThreadsInserted.incrementAndGet();

                  if (threadInserted % 1000 == 0)
                    LogManager.instance()
                        .log(this, Level.INFO, "%s Thread %d inserted %d records with key %d (total=%d)", null, getClass(), Thread.currentThread().getId(), i,
                            threadInserted, crossThreadsInserted.get());

                  keyPresent = true;

                } catch (NeedRetryException e) {
                  needRetryExceptions.incrementAndGet();
                  Assertions.assertFalse(database.isTransactionActive());
                  continue;
                } catch (DuplicatedKeyException e) {
                  duplicatedExceptions.incrementAndGet();
                  keyPresent = true;
                  Assertions.assertFalse(database.isTransactionActive());
                } catch (Exception e) {
                  LogManager.instance().log(this, Level.SEVERE, "%s Thread %d Generic Exception", e, getClass(), Thread.currentThread().getId());
                  Assertions.assertFalse(database.isTransactionActive());
                  return;
                }
              }

              if (!keyPresent)
                LogManager.instance().log(this, Level.WARNING, "%s Thread %d Cannot create key %d after %d retries! (total=%d)", null, getClass(),
                    Thread.currentThread().getId(), i, maxRetries, crossThreadsInserted.get());

            }

            LogManager.instance()
                .log(this, Level.INFO, "%s Thread %d completed (inserted=%d)", null, getClass(), Thread.currentThread().getId(), threadInserted);

          } catch (Exception e) {
            LogManager.instance().log(this, Level.SEVERE, "%s Thread %d Error", e, getClass(), Thread.currentThread().getId());
          }
        }

      });
    }

    for (int i = 0; i < threads.length; ++i)
      threads[i].start();

    for (int i = 0; i < threads.length; ++i) {
      try {
        threads[i].join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    LogManager.instance()
        .log(this, Level.INFO, "%s Completed (inserted=%d needRetryExceptions=%d duplicatedExceptions=%d)", null, getClass(), crossThreadsInserted.get(),
            needRetryExceptions.get(), duplicatedExceptions.get());

    if (total != crossThreadsInserted.get()) {
      LogManager.instance().log(this, Level.INFO, "DUMP OF INSERTED RECORDS (ORDERED BY ID)");
      final ResultSet resultset = database
          .query("sql", "select id, count(*) as total from ( select from " + TYPE_NAME + " group by id ) where total > 1 order by id");
      while (resultset.hasNext())
        LogManager.instance().log(this, Level.INFO, "- %s", null, resultset.next());

      LogManager.instance().log(this, Level.INFO, "COUNT OF INSERTED RECORDS (ORDERED BY ID)");
      final Map<Integer, Integer> result = new HashMap<>();
      database.scanType(TYPE_NAME, true, new DocumentCallback() {
        @Override
        public boolean onRecord(Document record) {
          final int id = (int) record.get("id");
          Integer key = result.get(id);
          if (key == null)
            result.put(id, 1);
          else
            result.put(id, key + 1);
          return true;
        }
      });

      LogManager.instance().log(this, Level.INFO, "FOUND %d ENTRIES", null, result.size());

      Iterator<Map.Entry<Integer, Integer>> it = result.entrySet().iterator();
      while (it.hasNext()) {
        Map.Entry<Integer, Integer> next = it.next();
        if (next.getValue() > 1)
          LogManager.instance().log(this, Level.INFO, "- %d = %d", null, next.getKey(), next.getValue());
      }
    }

    Assertions.assertEquals(total, crossThreadsInserted.get());
//    Assertions.assertTrue(needRetryExceptions.get() > 0);
    Assertions.assertTrue(duplicatedExceptions.get() > 0);

    Assertions.assertEquals(startingWith + total, database.countType(TYPE_NAME, true));
  }

  protected void beginTest() {
    database.transaction(new Database.TransactionScope() {
      @Override
      public void execute(Database database) {
        Assertions.assertFalse(database.getSchema().existsType(TYPE_NAME));

        final DocumentType type = database.getSchema().createDocumentType(TYPE_NAME, 3);
        type.createProperty("id", Integer.class);
        final Index[] indexes = database.getSchema().createIndexes(SchemaImpl.INDEX_TYPE.LSM_TREE, true, TYPE_NAME, new String[] { "id" }, PAGE_SIZE);

        for (int i = 0; i < TOT; ++i) {
          final MutableDocument v = database.newDocument(TYPE_NAME);
          v.set("id", i);
          v.set("name", "Jay");
          v.set("surname", "Miner");

          v.save();
        }

        database.commit();
        database.begin();

        for (Index index : indexes) {
          Assertions.assertTrue(index.getStats().get("pages") > 1);
        }
      }
    });
  }
}