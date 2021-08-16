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
package com.arcadedb.query.sql.method.misc;

import com.arcadedb.database.Identifiable;
import com.arcadedb.query.sql.executor.CommandContext;
import com.arcadedb.utility.FileUtils;

/**
 * @author Luca Garulli (l.garulli--(at)--gmail.com)
 */
public class SQLMethodLastIndexOf extends OAbstractSQLMethod {

  public static final String NAME = "lastindexof";

  public SQLMethodLastIndexOf() {
    super(NAME, 1, 2);
  }

  @Override
  public Object execute( final Object iThis, Identifiable iCurrentRecord, CommandContext iContext,
      Object ioResult, Object[] iParams) {
    final String toFind = FileUtils.getStringContent(iParams[0].toString());
    return iParams.length > 1 ?
        iThis.toString().lastIndexOf(toFind, Integer.parseInt(iParams[1].toString())) :
        iThis.toString().lastIndexOf(toFind);
  }
}