/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.index;

import com.arcadedb.database.RID;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface Index {
  String getName();

  void compact() throws IOException;

  /**
   * The returning iterator does not skip deleted entries.
   */
  IndexCursor iterator(Object[] fromKeys) throws IOException;

  /**
   * The returning iterator does not skip deleted entries.
   */
  IndexCursor iterator(boolean ascendingOrder) throws IOException;

  /**
   * The returning iterator does not skip deleted entries.
   */
  IndexCursor iterator(boolean ascendingOrder, Object[] fromKeys) throws IOException;

  /**
   * The returning iterator does not skip deleted entries.
   */
  IndexCursor range(Object[] beginKeys, Object[] endKeys) throws IOException;

  List<RID> get(Object[] keys);

  void put(Object[] keys, RID rid);

  void remove(Object[] keys);

  Map<String, Long> getStats();

  int getFileId();
}