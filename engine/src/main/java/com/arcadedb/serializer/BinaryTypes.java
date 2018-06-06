/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.serializer;

import com.arcadedb.database.Binary;
import com.arcadedb.database.RID;
import com.arcadedb.engine.MurmurHash;
import com.arcadedb.exception.DatabaseMetadataException;

import java.math.BigDecimal;
import java.util.Date;

public class BinaryTypes {
  public final static byte TYPE_NULL           = 0;
  public final static byte TYPE_STRING         = 1;
  public final static byte TYPE_BYTE           = 2;
  public final static byte TYPE_SHORT          = 3;
  public final static byte TYPE_INT            = 4;
  public final static byte TYPE_LONG           = 5;
  public final static byte TYPE_FLOAT          = 6;
  public final static byte TYPE_DOUBLE         = 7;
  public final static byte TYPE_DATE           = 8;
  public final static byte TYPE_DATETIME       = 9;
  public final static byte TYPE_DECIMAL        = 10;
  public final static byte TYPE_BOOLEAN        = 11;
  public final static byte TYPE_BINARY         = 12;
  public final static byte TYPE_COMPRESSED_RID = 13;
  public final static byte TYPE_RID            = 14;

  public static byte getTypeFromValue(final Object value) {
    final byte type;

    if (value == null)
      type = TYPE_NULL;
    else if (value instanceof String)
      type = TYPE_STRING;
    else if (value instanceof Byte)
      type = TYPE_BYTE;
    else if (value instanceof Short)
      type = TYPE_SHORT;
    else if (value instanceof Integer)
      type = TYPE_INT;
    else if (value instanceof Long)
      type = TYPE_LONG;
    else if (value instanceof Float)
      type = TYPE_FLOAT;
    else if (value instanceof Double)
      type = TYPE_DOUBLE;
    else if (value instanceof Date) // CAN'T DETERMINE IF DATE OR DATETIME, USE DATETIME
      type = TYPE_DATETIME;
    else if (value instanceof BigDecimal)
      type = TYPE_DECIMAL;
    else if (value instanceof Boolean)
      type = TYPE_BOOLEAN;
    else if (value instanceof byte[])
      type = TYPE_BINARY;
    else if (value instanceof RID)
      type = TYPE_COMPRESSED_RID;
    else
      throw new DatabaseMetadataException("Cannot serialize value '" + value + "' of type " + value.getClass());

    return type;
  }

  public static int getTypeSize(final byte type) {
    switch (type) {
    case BinaryTypes.TYPE_INT:
      return Binary.INT_SERIALIZED_SIZE;

    case BinaryTypes.TYPE_SHORT:
      return Binary.SHORT_SERIALIZED_SIZE;

    case BinaryTypes.TYPE_LONG:
    case BinaryTypes.TYPE_DATETIME:
    case BinaryTypes.TYPE_DATE:
      return Binary.LONG_SERIALIZED_SIZE;

    case BinaryTypes.TYPE_BYTE:
      return Binary.BYTE_SERIALIZED_SIZE;

    case BinaryTypes.TYPE_DECIMAL:

    case BinaryTypes.TYPE_FLOAT:
      return Binary.FLOAT_SERIALIZED_SIZE;

    case BinaryTypes.TYPE_DOUBLE:
      return Binary.DOUBLE_SERIALIZED_SIZE;

    case BinaryTypes.TYPE_RID:
      return Binary.INT_SERIALIZED_SIZE + Binary.LONG_SERIALIZED_SIZE;

    default:
      return -1;
    }
  }

  public static byte getTypeFromClass(final Class clazz) {
    final byte type;

    if (clazz == String.class)
      type = TYPE_STRING;
    else if (clazz == Byte.class)
      type = TYPE_BYTE;
    else if (clazz == Short.class)
      type = TYPE_SHORT;
    else if (clazz == Integer.class)
      type = TYPE_INT;
    else if (clazz == Long.class)
      type = TYPE_LONG;
    else if (clazz == Float.class)
      type = TYPE_FLOAT;
    else if (clazz == Double.class)
      type = TYPE_DOUBLE;
    else if (clazz == Date.class) // CAN'T DETERMINE IF DATE OR DATETIME, USE DATETIME
      type = TYPE_DATETIME;
    else if (clazz == BigDecimal.class)
      type = TYPE_DECIMAL;
    else if (clazz == Boolean.class)
      type = TYPE_BOOLEAN;
    else if (clazz == byte[].class)
      type = TYPE_BINARY;
    else if (clazz == RID.class)
      type = TYPE_COMPRESSED_RID;
    else
      throw new DatabaseMetadataException("Cannot find type for class '" + clazz + "'");

    return type;
  }

  public static int getHash(final Object[] keys) {
    return getHash(keys, keys.length);
  }

  public static int getHash(final Object[] keys, final int keyCount) {
    int hash = 0;

    for (int i = 0; i < keyCount; ++i)
      hash += getHash(keys[i]);

    return hash;
  }

  public static int getHash(final Object key) {
    final Class<? extends Object> clazz = key.getClass();

    if (clazz == String.class)
      return MurmurHash.hash32((String) key);
    else if (clazz == byte[].class)
      return MurmurHash.hash32(((byte[]) key), 0, ((byte[]) key).length);

    return key.hashCode();
  }
}