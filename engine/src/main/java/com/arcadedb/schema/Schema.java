/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.schema;

import com.arcadedb.engine.Bucket;
import com.arcadedb.engine.Dictionary;
import com.arcadedb.engine.PaginatedComponent;
import com.arcadedb.index.Index;

import java.util.Collection;
import java.util.TimeZone;

public interface Schema {

  void setDateTimeFormat(String dateTimeFormat);

  PaginatedComponent getFileById(int id);

  void removeFile(int fileId);

  boolean existsBucket(String bucketName);

  Bucket getBucketByName(String name);

  Collection<Bucket> getBuckets();

  Bucket getBucketById(int id);

  Bucket createBucket(String bucketName);

  boolean existsIndex(String indexName);

  Index[] getIndexes();

  Index getIndexByName(String indexName);

  Index[] createClassIndexes(boolean unique, String typeName, String[] propertyNames);

  Index[] createClassIndexes(boolean unique, String typeName, String[] propertyNames, int pageSize);

  Index createManualIndex(boolean unique, String indexName, byte[] keyTypes, int pageSize);

  Dictionary getDictionary();

  Collection<DocumentType> getTypes();

  DocumentType getType(String typeName);

  String getTypeNameByBucketId(int bucketId);

  DocumentType getTypeByBucketId(int bucketId);

  boolean existsType(String typeName);

  DocumentType createDocumentType(String typeName);

  DocumentType createDocumentType(String typeName, int buckets);

  DocumentType createDocumentType(String typeName, int buckets, int pageSize);

  VertexType createVertexType(String typeName);

  VertexType createVertexType(String typeName, int buckets);

  VertexType createVertexType(String typeName, int buckets, int pageSize);

  EdgeType createEdgeType(String typeName);

  EdgeType createEdgeType(String typeName, int buckets);

  EdgeType createEdgeType(String typeName, int buckets, int pageSize);

  TimeZone getTimeZone();

  void setTimeZone(TimeZone timeZone);

  String getDateFormat();

  void setDateFormat(String dateFormat);

  String getDateTimeFormat();

  String getEncoding();
}