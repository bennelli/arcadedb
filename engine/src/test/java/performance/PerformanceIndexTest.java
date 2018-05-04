package performance;

import com.arcadedb.database.*;
import com.arcadedb.database.async.PErrorCallback;
import com.arcadedb.engine.PPaginatedFile;
import com.arcadedb.schema.PDocumentType;
import org.junit.jupiter.api.Assertions;

public class PerformanceIndexTest {
  private static final int    TOT       = 5000000;
  private static final String TYPE_NAME = "Person";

  public static void main(String[] args) throws Exception {
    new PerformanceIndexTest().run();
  }

  private void run() {
    PerformanceTest.clean();

    final int parallel = 2;

    PDatabase database = new PDatabaseFactory(PerformanceTest.DATABASE_PATH, PPaginatedFile.MODE.READ_WRITE).acquire();
    try {
      if (!database.getSchema().existsType(TYPE_NAME)) {
        database.begin();

        final PDocumentType type = database.getSchema().createDocumentType(TYPE_NAME, parallel);

        type.createProperty("id", Long.class);
        type.createProperty("name", String.class);
        type.createProperty("surname", String.class);
        type.createProperty("locali", Integer.class);
        type.createProperty("notes1", String.class);
        type.createProperty("notes2", String.class);

        database.getSchema().createClassIndexes(TYPE_NAME, new String[] { "id" }, 5000000);
        database.commit();
      }
    } finally {
      database.close();
    }

    database = new PDatabaseFactory(PerformanceTest.DATABASE_PATH, PPaginatedFile.MODE.READ_WRITE).acquire();

    long begin = System.currentTimeMillis();

    try {

      database.asynch().setCommitEvery(5000);
      database.asynch().setParallelLevel(parallel);
      database.asynch().setTransactionUseWAL(true);

      database.asynch().onError(new PErrorCallback() {
        @Override
        public void call(Exception exception) {
          System.out.println("ERROR: " + exception);
          exception.printStackTrace();
        }
      });

      long row = 0;
      for (; row < TOT; ++row) {
        final PModifiableDocument record = database.newDocument(TYPE_NAME);

        record.set("id", row);
        record.set("name", "Luca" + row);
        record.set("surname", "Skywalker" + row);
        record.set("locali", 10);
        record.set("notes1",
            "This is a long field to check how Arcade behaves with large fields. This is a long field to check how Arcade behaves with large fields. This is a long field to check how Arcade behaves with large fields. This is a long field to check how Arcade behaves with large fields.");
        record.set("notes2",
            "This is a long field to check how Arcade behaves with large fields. This is a long field to check how Arcade behaves with large fields. This is a long field to check how Arcade behaves with large fields. This is a long field to check how Arcade behaves with large fields.");

        database.asynch().createRecord(record);

        if (row % 100000 == 0)
          System.out.println("Written " + row + " elements in " + (System.currentTimeMillis() - begin) + "ms");
      }

      System.out.println("Inserted " + row + " elements in " + (System.currentTimeMillis() - begin) + "ms");

    } finally {
      database.close();
      System.out.println("Insertion finished in " + (System.currentTimeMillis() - begin) + "ms");
    }

    begin = System.currentTimeMillis();
    database = new PDatabaseFactory(PerformanceTest.DATABASE_PATH, PPaginatedFile.MODE.READ_ONLY).acquire();
    try {
      System.out.println("Lookup all the keys...");
      for (long id = 0; id < TOT; ++id) {
        final PCursor<PRID> records = database.lookupByKey(TYPE_NAME, new String[] { "id" }, new Object[] { id });
        Assertions.assertNotNull(records);
        Assertions.assertEquals(1, records.size(), "Wrong result for lookup of key " + id);

        final PDocument record = (PDocument) records.next().getRecord();
        Assertions.assertEquals(id, record.get("id"));

        if (id % 100000 == 0)
          System.out.println("Checked " + id + " lookups in " + (System.currentTimeMillis() - begin) + "ms");
      }
    } finally {
      database.close();
      System.out.println("Lookup finished in " + (System.currentTimeMillis() - begin) + "ms");
    }

  }
}