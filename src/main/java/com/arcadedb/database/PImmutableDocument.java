package com.arcadedb.database;

import java.util.Map;
import java.util.Set;

/**
 * Immutable document implementation. To modify the content, call modify() to obtain a modifiable copy.
 */
public class PImmutableDocument extends PBaseDocument {

  protected PImmutableDocument(final PDatabase graph, final String typeName, final PRID rid, final PBinary buffer) {
    super(graph, typeName, rid, buffer);
  }

  @Override
  public Object get(final String name) {
    checkForLazyLoading();
    buffer.position(propertiesStartingPosition);
    final Map<String, Object> map = database.getSerializer().deserializeProperties(database, buffer, name);
    return map.get(name);
  }

  @Override
  public PModifiableDocument modify() {
    checkForLazyLoading();
    return new PModifiableDocument(database, typeName, rid, buffer);
  }

  @Override
  public String toString() {
    final StringBuilder buffer = new StringBuilder(256);
    if (rid != null)
      buffer.append(rid);
    buffer.append('[');
    int i = 0;
    if (buffer != null) {
      for (String name : getPropertyNames()) {
        if (i > 0)
          buffer.append(',');

        buffer.append(name);
        buffer.append('=');
        buffer.append(get(name));
        i++;
      }
      buffer.append(']');
    }
    return buffer.toString();
  }

  @Override
  public Set<String> getPropertyNames() {
    checkForLazyLoading();

    buffer.position(1); // SKIP RECORD BYTE

    return database.getSerializer().getPropertyNames(database, buffer);
  }

  protected boolean checkForLazyLoading() {
    if (buffer == null) {
      if (rid == null)
        throw new RuntimeException("Document cannot be loaded because RID is null");

      buffer = database.getSchema().getBucketById(rid.getBucketId()).getRecord(rid);
      return true;
    }
    return false;
  }
}
