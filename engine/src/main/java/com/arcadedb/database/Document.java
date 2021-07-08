/*
 * Copyright 2021 Arcade Data Ltd
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.arcadedb.database;

import com.arcadedb.graph.EmbeddedDocument;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public interface Document extends Record {
  byte RECORD_TYPE = 0;

  MutableDocument modify();

  DetachedDocument detach();

  boolean has(String propertyName);

  Object get(String propertyName);

  String getString(String propertyName);

  Boolean getBoolean(String propertyName);

  Byte getByte(String propertyName);

  Short getShort(String propertyName);

  Integer getInteger(String propertyName);

  Long getLong(String propertyName);

  Float getFloat(String propertyName);

  Double getDouble(String propertyName);

  BigDecimal getDecimal(String propertyName);

  Date getDate(String propertyName);

  EmbeddedDocument getEmbedded(String propertyName);

  Set<String> getPropertyNames();

  String getType();

  JSONObject toJSON();

  Map<String, Object> toMap();
}
