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
package com.arcadedb.sql.method.misc;

import com.arcadedb.database.Identifiable;
import com.arcadedb.sql.executor.CommandContext;

import java.util.*;

/**
 * Transforms current value in a List.
 *
 * @author Luca Garulli (l.garulli--(at)--orientdb.com)
 */
public class SQLMethodAsList extends OAbstractSQLMethod {

  public static final String NAME = "aslist";

  public SQLMethodAsList() {
    super(NAME);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object execute( final Object iThis, Identifiable iCurrentRecord, CommandContext iContext,
      Object ioResult, Object[] iParams) {
    if (ioResult instanceof List)
      // ALREADY A LIST
      return ioResult;

    if (ioResult == null)
      // NULL VALUE, RETURN AN EMPTY LIST
      return Collections.EMPTY_LIST;

    if (ioResult instanceof Collection<?>) {
      return new ArrayList<>((Collection<Object>) ioResult);
    } else if (ioResult instanceof Iterable<?>) {
      ioResult = ((Iterable<?>) ioResult).iterator();
    }

    if (ioResult instanceof Iterator<?>) {
      final List<Object> list = new ArrayList<>();

      for (Iterator<Object> iter = (Iterator<Object>) ioResult; iter.hasNext(); ) {
        list.add(iter.next());
      }
      return list;
    }

    // SINGLE ITEM: ADD IT AS UNIQUE ITEM
    return Collections.singletonList(ioResult);
  }
}
