/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.database;

import com.arcadedb.engine.Bucket;
import com.arcadedb.engine.TransactionManager;
import com.arcadedb.engine.WALFileFactory;
import com.arcadedb.graph.GraphEngine;
import com.arcadedb.schema.DocumentType;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Internal API, do not use as an end user.
 */
public interface DatabaseInternal extends Database {
  enum CALLBACK_EVENT {
    TX_LAST_OP, TX_AFTER_WAL_WRITE, DB_NOT_CLOSED
  }

  void registerCallback(CALLBACK_EVENT event, Callable<Void> callback);

  void unregisterCallback(CALLBACK_EVENT event, Callable<Void> callback);

  void executeCallbacks(CALLBACK_EVENT event) throws IOException;

  GraphEngine getGraphEngine();

  TransactionManager getTransactionManager();

  void createRecord(ModifiableDocument record);

  void createRecord(Record record, String bucketName);

  void createRecordNoLock(Record record, String bucketName);

  void updateRecord(Record record);

  void updateRecordNoLock(Record record);

  void indexDocument(ModifiableDocument record, DocumentType type, Bucket bucket);

  void kill();

  WALFileFactory getWALFileFactory();
}