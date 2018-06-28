/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.engine;

import com.arcadedb.database.Binary;

/**
 * Base interface for compression.
 */
public interface Compression {
  Binary compress(Binary data);

  Binary decompress(Binary data);
}