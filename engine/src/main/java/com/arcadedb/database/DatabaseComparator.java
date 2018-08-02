/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.database;

import com.arcadedb.engine.BasePage;
import com.arcadedb.engine.Bucket;
import com.arcadedb.engine.PageId;
import com.arcadedb.schema.DocumentType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

/**
 * Compares 2 databases if are identical.
 */
public class DatabaseComparator {

  static public class DatabaseAreNotIdentical extends RuntimeException {
    public DatabaseAreNotIdentical(final String text, final Object... args) {
      super(String.format(text, args));
    }
  }

  public void compare(final Database db1, final Database db2) {
    compareTypes(db1, db2);
    compareBuckets(db1, db2);
  }

  public void compareBuckets(final Database db1, final Database db2) {
    final Collection<Bucket> buckets1 = db1.getSchema().getBuckets();
    final Collection<Bucket> buckets2 = db2.getSchema().getBuckets();
    if (buckets1.size() != buckets2.size())
      throw new DatabaseAreNotIdentical("Buckets: DB1 %d <> DB2 %d", buckets1.size(), buckets2.size());

    final HashMap<String, Bucket> types1Map = new HashMap<>(buckets1.size());
    final HashMap<String, Bucket> types2Map = new HashMap<>(buckets2.size());

    for (Bucket entry : buckets1)
      types1Map.put(entry.getName(), entry);

    for (Bucket entry : buckets2) {
      if (!types1Map.containsKey(entry.getName()))
        throw new DatabaseAreNotIdentical("Bucket '%s' is not present in DB2", entry.getName());
      types2Map.put(entry.getName(), entry);
    }

    for (Bucket entry : buckets1)
      if (!types2Map.containsKey(entry.getName()))
        throw new DatabaseAreNotIdentical("Bucket '%s' is not present in DB1", entry.getName());

    for (Bucket bucket1 : buckets1) {
      final Bucket bucket2 = types2Map.get(bucket1.getName());

      if (bucket1.getPageSize() != bucket2.getPageSize())
        throw new DatabaseAreNotIdentical("Bucket '%s' has different page size in two databases. DB1 %d <> DB2 %d", bucket2.getName(), bucket1.getPageSize(),
            bucket2.getPageSize());

      if (bucket1.getTotalPages() != bucket2.getTotalPages())
        throw new DatabaseAreNotIdentical("Bucket '%s' has different page count in two databases. DB1 %d <> DB2 %d", bucket2.getName(), bucket1.getTotalPages(),
            bucket2.getTotalPages());

      // AT THIS POINT BOTH BUCKETS HAVE THE SAME PAGES
      final int pageSize = bucket1.getPageSize();
      for (int i = 0; i < bucket1.getTotalPages(); ++i) {
        final PageId pageId = new PageId(bucket1.getId(), i);

        final BasePage page1;
        final BasePage page2;

        try {
          page1 = db1.getPageManager().getPage(pageId, pageSize, false);
        } catch (IOException e) {
          throw new DatabaseAreNotIdentical("Error on reading page %s from bucket '%s' DB1 (cause=%s)", pageId, bucket1.getName(), e.toString());
        }

        try {
          page2 = db2.getPageManager().getPage(pageId, pageSize, false);
        } catch (IOException e) {
          throw new DatabaseAreNotIdentical("Error on reading page %s from bucket '%s' DB2 (cause=%s)", pageId, bucket2.getName(), e.toString());
        }

        if (page1.getVersion() != page2.getVersion())
          throw new DatabaseAreNotIdentical("Page %s has different versions on databases. DB1 %d <> DB2 %d", pageId, page1.getVersion(), page2.getVersion());

        if (!Arrays.equals(page1.getContent().array(), page2.getContent().array()))
          throw new DatabaseAreNotIdentical("Page %s has different content on databases", pageId);
      }
    }

  }

  public void compareTypes(final Database db1, final Database db2) {
    final Collection<DocumentType> types1 = db1.getSchema().getTypes();
    final Collection<DocumentType> types2 = db2.getSchema().getTypes();
    if (types1.size() != types2.size())
      throw new DatabaseAreNotIdentical("Types: DB1 %d <> DB2 %d", types1.size(), types2.size());

    final HashMap<String, DocumentType> types1Map = new HashMap<>(types1.size());
    final HashMap<String, DocumentType> types2Map = new HashMap<>(types2.size());

    for (DocumentType entry : types1)
      types1Map.put(entry.getName(), entry);

    for (DocumentType entry : types2) {
      if (!types1Map.containsKey(entry.getName()))
        throw new DatabaseAreNotIdentical("Types '%s' is not present in DB2", entry.getName());
      types2Map.put(entry.getName(), entry);
    }

    for (DocumentType entry : types1)
      if (!types2Map.containsKey(entry.getName()))
        throw new DatabaseAreNotIdentical("Types '%s' is not present in DB1", entry.getName());

    // AT THIS POINT BOTH DBS HAVE THE SAME TYPE NAMES, CHECKING TYPE DETAILS
    for (DocumentType entry1 : types1) {
      final DocumentType entry2 = types2Map.get(entry1.getName());
      if (!entry1.isTheSameAs(entry2))
        throw new DatabaseAreNotIdentical("Types '%s' is configured differently in two databases", entry2.getName());
    }
  }
}