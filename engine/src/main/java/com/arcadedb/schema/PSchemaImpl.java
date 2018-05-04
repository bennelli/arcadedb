package com.arcadedb.schema;

import com.arcadedb.PConstants;
import com.arcadedb.database.PDatabase;
import com.arcadedb.database.PDatabaseInternal;
import com.arcadedb.engine.PBucket;
import com.arcadedb.engine.PDictionary;
import com.arcadedb.engine.PPaginatedComponent;
import com.arcadedb.engine.PPaginatedFile;
import com.arcadedb.exception.PConfigurationException;
import com.arcadedb.exception.PDatabaseMetadataException;
import com.arcadedb.exception.PSchemaException;
import com.arcadedb.index.PIndex;
import com.arcadedb.index.PIndexLSM;
import com.arcadedb.serializer.PBinaryTypes;
import com.arcadedb.utility.PLogManager;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;

public class PSchemaImpl implements PSchema {
  private static final String                     SCHEMA_FILE_NAME = "/schema.pcsv";
  private final        PDatabaseInternal          database;
  private final        List<PPaginatedComponent>  files            = new ArrayList<PPaginatedComponent>();
  private final        Map<String, PDocumentType> types            = new HashMap<String, PDocumentType>();
  private final        Map<String, PBucket>       bucketMap        = new HashMap<String, PBucket>();
  private final        Map<String, PIndex>        indexMap         = new HashMap<String, PIndex>();
  private final        String                     databasePath;
  private              PDictionary                dictionary;

  public PSchemaImpl(final PDatabaseInternal database, final String databasePath, final PPaginatedFile.MODE mode) {
    this.database = database;
    this.databasePath = databasePath;
  }

  public void create(final PPaginatedFile.MODE mode) {
    database.begin();
    try {
      dictionary = new PDictionary(database, "dictionary", databasePath + "/dictionary", mode, PDictionary.DEF_PAGE_SIZE);
      files.add(dictionary);

      database.commit();

    } catch (Exception e) {
      PLogManager.instance().error(this, "Error on opening dictionary '%s' (error=%s)", e, databasePath, e.toString());
      database.rollback();
      throw new PDatabaseMetadataException("Error on loading dictionary (error=" + e.toString() + ")", e);
    }
  }

  public void load(final PPaginatedFile.MODE mode) {
    for (PPaginatedFile file : database.getFileManager().getFiles()) {
      final String fileName = file.getFileName();
      final int fileId = file.getFileId();
      final String fileExt = file.getFileExtension();
      final int pageSize = file.getPageSize();

      PPaginatedComponent pf = null;

      if (fileExt.equals(PDictionary.DICT_EXT)) {
        // DICTIONARY
        try {
          dictionary = new PDictionary(database, fileName, file.getFilePath(), fileId, mode, pageSize);
          pf = dictionary;

        } catch (IOException e) {
          PLogManager.instance().error(this, "Error on opening dictionary '%s' (error=%s)", e, file, e.toString());
        }

      } else if (fileExt.equals(PBucket.BUCKET_EXT)) {
        // BUCKET
        try {
          final PBucket newPart = new PBucket(database, fileName, file.getFilePath(), fileId, mode, pageSize);
          bucketMap.put(fileName, newPart);
          pf = newPart;
        } catch (IOException e) {
          PLogManager.instance().error(this, "Error on opening bucket '%s' (error=%s)", e, file, e.toString());
        }

      } else if (fileExt.equals(PIndexLSM.INDEX_EXT)) {
        // INDEX
        try {
          final PIndexLSM index = new PIndexLSM(database, fileName, file.getFilePath(), fileId, mode, pageSize);
          indexMap.put(fileName, index);
          pf = index;
        } catch (IOException e) {
          PLogManager.instance().error(this, "Error on opening index '%s' (error=%s)", e, file, e.toString());
        }
      }

      if (pf != null)
        registerFile(pf);
    }

    readConfiguration();
  }

  @Override
  public PPaginatedComponent getFileById(final int id) {
    if (id >= files.size())
      throw new PSchemaException("File with id '" + id + "' was not found");

    final PPaginatedComponent p = files.get(id);
    if (p == null)
      throw new PSchemaException("File with id '" + id + "' was not found");
    return p;
  }

  @Override
  public void removeFile(final int fileId) {
    if (fileId >= files.size())
      return;
    files.set(fileId, null);
  }

  @Override
  public Collection<PBucket> getBuckets() {
    return Collections.unmodifiableCollection(bucketMap.values());
  }

  public boolean existsBucket(final String bucketName) {
    return bucketMap.containsKey(bucketName);
  }

  @Override
  public PBucket getBucketByName(final String name) {
    final PBucket p = bucketMap.get(name);
    if (p == null)
      throw new PSchemaException("Bucket with name '" + name + "' was not found");
    return p;
  }

  @Override
  public PBucket getBucketById(final int id) {
    if (id < 0 || id >= files.size())
      throw new PSchemaException("Bucket with id '" + id + "' was not found");

    final PPaginatedComponent p = files.get(id);
    if (p == null || !(p instanceof PBucket))
      throw new PSchemaException("Bucket with id '" + id + "' was not found");
    return (PBucket) p;
  }

  @Override
  public PBucket createBucket(final String bucketName) {
    return createBucket(bucketName, PBucket.DEF_PAGE_SIZE);
  }

  public PBucket createBucket(final String bucketName, final int pageSize) {
    return (PBucket) database.executeInWriteLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        if (bucketMap.containsKey(bucketName))
          throw new PSchemaException("Cannot create bucket '" + bucketName + "' because already exists");

        try {
          final PBucket bucket = new PBucket(database, bucketName, databasePath + "/" + bucketName, PPaginatedFile.MODE.READ_WRITE,
              pageSize);
          registerFile(bucket);
          bucketMap.put(bucketName, bucket);

          return bucket;

        } catch (IOException e) {
          throw new PSchemaException("Cannot create bucket '" + bucketName + "' (error=" + e + ")", e);
        }
      }
    });
  }

  @Override
  public boolean existsIndex(final String indexName) {
    return indexMap.containsKey(indexName);
  }

  @Override
  public PIndex[] getIndexes() {
    final PIndex[] indexes = new PIndex[indexMap.size()];
    int i = 0;
    for (PIndex index : indexMap.values())
      indexes[i++] = index;
    return indexes;
  }

  @Override
  public PIndex getIndexByName(final String indexName) {
    final PIndex p = indexMap.get(indexName);
    if (p == null)
      throw new PSchemaException("Index with name '" + indexName + "' was not found");
    return p;
  }

  @Override
  public PIndex[] createClassIndexes(final String typeName, final String[] propertyNames) {
    return createClassIndexes(typeName, propertyNames, PIndexLSM.DEF_PAGE_SIZE);
  }

  @Override
  public PIndex[] createClassIndexes(final String typeName, final String[] propertyNames, final int pageSize) {
    return createClassIndexes(typeName, propertyNames, pageSize, propertyNames.length);
  }

  public PIndex[] createClassIndexes(final String typeName, final String[] propertyNames, final int pageSize,
      final int bfKeyDepth) {
    return (PIndex[]) database.executeInWriteLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        try {
          if (propertyNames.length == 0)
            throw new PDatabaseMetadataException(
                "Cannot create index on type '" + typeName + "' because there are no property defined");

          final PDocumentType type = getType(typeName);

          // CHECK ALL THE PROPERTIES EXIST
          final byte[] keyTypes = new byte[propertyNames.length];
          int i = 0;

          for (String propertyName : propertyNames) {
            final PProperty property = type.getPolymorphicProperty(propertyName);
            if (property == null)
              throw new PSchemaException(
                  "Cannot create the index on type '" + typeName + "." + propertyName + "' because the property does not exist");

            keyTypes[i++] = PBinaryTypes.getTypeFromClass(property.getType());
          }

          final List<PBucket> buckets = type.getBuckets(false);

          final PIndexLSM[] indexes = new PIndexLSM[buckets.size()];
          for (int idx = 0; idx < buckets.size(); ++idx) {
            final PBucket b = buckets.get(idx);
            final String indexName = b.getName() + "_" + System.currentTimeMillis();

            if (indexMap.containsKey(indexName))
              throw new PDatabaseMetadataException(
                  "Cannot create index '" + indexName + "' on type '" + typeName + "' because it already exists");

            indexes[idx] = new PIndexLSM(database, indexName, databasePath + "/" + indexName, PPaginatedFile.MODE.READ_WRITE,
                keyTypes, PBinaryTypes.TYPE_RID, pageSize, bfKeyDepth);

            registerFile(indexes[idx]);
            indexMap.put(indexName, indexes[idx]);

            type.addIndexInternal(indexes[idx], b, propertyNames);
          }

          saveConfiguration();

          return indexes;

        } catch (IOException e) {
          throw new PSchemaException("Cannot create index on type '" + typeName + "' (error=" + e + ")", e);
        }
      }
    });
  }

  public PIndex createManualIndex(final String indexName, final byte[] keyTypes, final int pageSize) {
    return createManualIndex(indexName, keyTypes, pageSize, keyTypes.length);
  }

  public PIndex createManualIndex(final String indexName, final byte[] keyTypes, final int pageSize, final int bfKeyDepth) {
    return (PIndexLSM) database.executeInWriteLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        if (indexMap.containsKey(indexName))
          throw new PSchemaException("Cannot create index '" + indexName + "' because already exists");

        try {
          final PIndexLSM index = new PIndexLSM(database, indexName, databasePath + "/" + indexName, PPaginatedFile.MODE.READ_WRITE,
              keyTypes, PBinaryTypes.TYPE_RID, pageSize, bfKeyDepth);
          registerFile(index);
          indexMap.put(indexName, index);

          return index;

        } catch (IOException e) {
          throw new PSchemaException("Cannot create index '" + indexName + "' (error=" + e + ")", e);
        }
      }
    });
  }

  public void close() {
    files.clear();
    types.clear();
    bucketMap.clear();
    indexMap.clear();
    dictionary = null;
  }

  public PDictionary getDictionary() {
    return dictionary;
  }

  public PDatabase getDatabase() {
    return database;
  }

  public Collection<PDocumentType> getTypes() {
    return Collections.unmodifiableCollection(types.values());
  }

  public PDocumentType getType(final String typeName) {
    final PDocumentType t = types.get(typeName);
    if (t == null)
      throw new PSchemaException("Type with name '" + typeName + "' was not found");
    return t;
  }

  @Override
  public String getTypeNameByBucketId(final int bucketId) {
    for (PDocumentType t : types.values()) {
      for (PBucket b : t.getBuckets(false)) {
        if (b.getId() == bucketId)
          return t.getName();
      }
    }

    // NOT FOUND
    return null;
  }

  @Override
  public PDocumentType getTypeByBucketId(final int bucketId) {
    for (PDocumentType t : types.values()) {
      for (PBucket b : t.getBuckets(false)) {
        if (b.getId() == bucketId)
          return t;
      }
    }

    // NOT FOUND
    return null;
  }

  public boolean existsType(final String typeName) {
    return types.containsKey(typeName);
  }

  public PDocumentType createDocumentType(final String typeName) {
    return createDocumentType(typeName, 1);
  }

  public PDocumentType createDocumentType(final String typeName, final int buckets) {
    return createDocumentType(typeName, buckets, PBucket.DEF_PAGE_SIZE);
  }

  public PDocumentType createDocumentType(final String typeName, final int buckets, final int pageSize) {
    return (PDocumentType) database.executeInWriteLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        if (typeName.indexOf(",") > -1)
          throw new IllegalArgumentException("Type name '" + typeName + "' contains non valid characters");

        if (types.containsKey(typeName))
          throw new PSchemaException("Type '" + typeName + "' already exists");
        final PDocumentType c = new PDocumentType(PSchemaImpl.this, typeName);
        types.put(typeName, c);

        for (int i = 0; i < buckets; ++i)
          c.addBucket(createBucket(typeName + "_" + i, pageSize));

        saveConfiguration();

        return c;
      }
    });
  }

  @Override
  public PVertexType createVertexType(final String typeName) {
    return createVertexType(typeName, 1);
  }

  @Override
  public PVertexType createVertexType(final String typeName, final int buckets) {
    return createVertexType(typeName, buckets, PBucket.DEF_PAGE_SIZE);
  }

  @Override
  public PVertexType createVertexType(String typeName, final int buckets, final int pageSize) {
    return (PVertexType) database.executeInWriteLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        if (typeName.indexOf(",") > -1)
          throw new IllegalArgumentException("Vertex type name '" + typeName + "' contains non valid characters");

        if (types.containsKey(typeName))
          throw new PSchemaException("Vertex type '" + typeName + "' already exists");
        final PDocumentType c = new PVertexType(PSchemaImpl.this, typeName);
        types.put(typeName, c);

        for (int i = 0; i < buckets; ++i)
          c.addBucket(createBucket(typeName + "_" + i, pageSize));

        database.getGraphEngine().createVertexType(database, c);

        saveConfiguration();

        return c;
      }
    });
  }

  @Override
  public PEdgeType createEdgeType(final String typeName) {
    return createEdgeType(typeName, 1);
  }

  @Override
  public PEdgeType createEdgeType(final String typeName, final int buckets) {
    return createEdgeType(typeName, buckets, PBucket.DEF_PAGE_SIZE);
  }

  @Override
  public PEdgeType createEdgeType(final String typeName, final int buckets, final int pageSize) {
    return (PEdgeType) database.executeInWriteLock(new Callable<Object>() {
      @Override
      public Object call() throws Exception {
        if (typeName.indexOf(",") > -1)
          throw new IllegalArgumentException("Edge type name '" + typeName + "' contains non valid characters");

        if (types.containsKey(typeName))
          throw new PSchemaException("Edge type '" + typeName + "' already exists");
        final PDocumentType c = new PEdgeType(PSchemaImpl.this, typeName, dictionary.getIdByName(typeName, true));
        types.put(typeName, c);

        for (int i = 0; i < buckets; ++i)
          c.addBucket(createBucket(typeName + "_" + i, pageSize));

        saveConfiguration();

        return c;
      }
    });
  }

  public void swapIndexes(final PIndexLSM oldIndex, final PIndexLSM newIndex) throws IOException {
    indexMap.remove(oldIndex.getName());

    indexMap.put(newIndex.getName(), newIndex);

    // SCAN ALL THE TYPES TO FIND WHERE THE INDEX WAS DEFINED TO REPLACE IT
    for (PDocumentType t : getTypes()) {
      for (List<PDocumentType.IndexMetadata> metadata : t.getAllIndexesMetadata()) {
        for (PDocumentType.IndexMetadata m : metadata) {
          if (m.index.equals(oldIndex)) {
            m.index = newIndex;
            break;
          }
        }
      }
    }

    newIndex.removeTempSuffix();

    oldIndex.drop();
  }

  protected void readConfiguration() {
    types.clear();

    final File file = new File(databasePath + SCHEMA_FILE_NAME);
    if (!file.exists())
      return;

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      int lineNum = 0;
      String line;

      PDocumentType lastType = null;

      final Map<String, String[]> parentTypes = new HashMap<>();

      while ((line = br.readLine()) != null) {
        if (lineNum == 0) {
          if (!line.startsWith("#ARCADEDB,"))
            throw new PConfigurationException("Format exception while parsing schema file: " + (databasePath + SCHEMA_FILE_NAME));
        } else if (!line.startsWith("+")) {
          // TYPE
          final String[] parts = line.split(",");

          final String typeName = parts[0];
          final String kind = parts[1];

          final PDocumentType type;
          if ("v".equals(kind)) {
            type = new PVertexType(this, typeName);
          } else if ("e".equals(kind)) {
            type = new PEdgeType(this, typeName, dictionary.getIdByName(typeName, false));
          } else {
            type = new PDocumentType(this, typeName);
          }

          // parts[1] // IGNORE STRATEGY FOR NOW
          final String[] parentItems = parts[3].isEmpty() ? null : parts[3].split(";");

          if (parentItems != null)
            // SAVE THE PARENT HIERARCHY FOR LATER
            parentTypes.put(typeName, parentItems);

          final String[] bucketItems = parts[4].split(";");
          for (String b : bucketItems) {
            final PPaginatedComponent bucket = files.get(Integer.parseInt(b));
            if (bucket == null || !(bucket instanceof PBucket))
              PLogManager.instance().warn(this, "Cannot find bucket %s for type '%s'", b, parts[0]);
            type.addBucketInternal((PBucket) bucket);
          }

          types.put(type.getName(), type);
          lastType = type;
        } else {
          // INDEX
          final String[] parts = line.substring(1).split(",");
          final String bucketName = parts[1];
          final String[] keys = parts[2].split(";");
          lastType.addIndexInternal(getIndexByName(parts[0]), bucketMap.get(bucketName), keys);
        }
        lineNum++;
      }

      // RESTORE THE INHERITANCE
      for (Map.Entry<String, String[]> entry : parentTypes.entrySet()) {
        final PDocumentType type = getType(entry.getKey());
        for (String p : entry.getValue())
          type.addParent(getType(p));
      }

    } catch (IOException e) {
      PLogManager.instance().error(this, "Error on loading schema configuration from file: %s", e, databasePath + SCHEMA_FILE_NAME);
    }
  }

  protected void saveConfiguration() {
    try {
      final FileWriter file = new FileWriter(databasePath + SCHEMA_FILE_NAME);

      file.append(String.format("#ARCADEDB,%s\n", PConstants.VERSION));
      for (PDocumentType t : types.values()) {

        final StringBuilder parentList2String = new StringBuilder();
        for (PDocumentType p : t.getParentTypes()) {
          if (parentList2String.length() > 0)
            parentList2String.append(';');
          parentList2String.append(p.getName());
        }

        final StringBuilder bucketList2String = new StringBuilder();
        for (PBucket b : t.getBuckets(false)) {
          if (bucketList2String.length() > 0)
            bucketList2String.append(';');
          bucketList2String.append(b.getId());
        }

        final String kind;
        if (t instanceof PVertexType)
          kind = "v";
        else if (t instanceof PEdgeType)
          kind = "e";
        else
          kind = "d";

        file.append(String
            .format("%s,%s,%s,%s,%s\n", t.getName(), kind, t.getSyncSelectionStrategy().getName(), parentList2String.toString(),
                bucketList2String.toString()));

        for (List<PDocumentType.IndexMetadata> list : t.getAllIndexesMetadata()) {
          for (PDocumentType.IndexMetadata entry : list) {
            final StringBuilder keys2String = new StringBuilder();
            for (String k : entry.propertyNames) {
              if (keys2String.length() > 0)
                keys2String.append(';');
              keys2String.append(k);
            }

            file.append(String
                .format("+%s,%s,%s\n", entry.index.getName(), getBucketById(entry.bucketId).getName(), keys2String.toString()));
          }
        }
      }
      file.close();

    } catch (IOException e) {
      PLogManager.instance().error(this, "Error on saving schema configuration to file: %s", e, databasePath + SCHEMA_FILE_NAME);
    }
  }

  public void registerFile(final PPaginatedComponent file) {
    while (files.size() < file.getId() + 1)
      files.add(null);

    if (files.get(file.getId()) != null)
      throw new PSchemaException("File with id '" + file.getId() + "' already exists (" + files.get(file.getId()) + ")");

    files.set(file.getId(), file);
  }
}