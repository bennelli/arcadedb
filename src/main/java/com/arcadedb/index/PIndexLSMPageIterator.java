package com.arcadedb.index;

import com.arcadedb.database.PBinary;
import com.arcadedb.engine.PBasePage;
import com.arcadedb.engine.PPageId;
import com.arcadedb.serializer.PBinarySerializer;

import java.io.IOException;

import static com.arcadedb.database.PBinary.INT_SERIALIZED_SIZE;

public class PIndexLSMPageIterator {
  private final PIndexLSM         index;
  private final PPageId           pageId;
  private final PBinary           buffer;
  private final byte[]            keyTypes;
  private final int               keyStartPosition;
  private final PBinarySerializer serializer;
  private final int               totalKeys;
  private final boolean           ascendingOrder;

  private int currentEntryIndex;
  private int valuePosition = -1;
  private Object[] nextKeys;
  private Object   nextValue;

  public PIndexLSMPageIterator(final PIndexLSM index, final PBasePage page, final int currentEntryInPage, final int keyStartPosition,
      final byte[] keyTypes, final int totalKeys, final boolean ascendingOrder) {
    this.index = index;
    this.pageId = page.getPageId();
    this.buffer = new PBinary(page.slice());
    this.keyStartPosition = keyStartPosition;
    this.keyTypes = keyTypes;
    this.serializer = index.getDatabase().getSerializer();
    this.totalKeys = totalKeys;
    this.currentEntryIndex = currentEntryInPage;
    this.ascendingOrder = ascendingOrder;
  }

  public boolean hasNext() throws IOException {
    if (ascendingOrder)
      return currentEntryIndex < totalKeys - 1;
    return currentEntryIndex > 0;
  }

  public void next() throws IOException {
    currentEntryIndex += ascendingOrder ? 1 : -1;
    nextKeys = null;
    nextValue = null;
  }

  public Object[] getKeys() {
    if (nextKeys != null)
      return nextKeys;

    final int contentPos = buffer.getInt(keyStartPosition + (currentEntryIndex * INT_SERIALIZED_SIZE));
    buffer.position(contentPos);

    nextKeys = new Object[keyTypes.length];
    for (int k = 0; k < keyTypes.length; ++k)
      nextKeys[k] = index.getDatabase().getSerializer().deserializeValue(index.getDatabase(), buffer, keyTypes[k]);

    valuePosition = buffer.position();
    nextValue = null;

    return nextKeys;
  }

  public Object getValue() {
    if (nextValue == null) {
      if (valuePosition < 0)
        getKeys();
      nextValue = index.getValue(buffer, serializer, valuePosition);
    }
    return nextValue;
  }

  public void close() {
  }

  public int getCurrentPosition() {
    return currentEntryIndex;
  }

  public int getTotalEntries() {
    return totalKeys;
  }
}