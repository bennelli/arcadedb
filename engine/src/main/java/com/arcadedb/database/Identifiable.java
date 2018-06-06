/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.database;

public interface Identifiable {
  RID getIdentity();

  Record getRecord();
}