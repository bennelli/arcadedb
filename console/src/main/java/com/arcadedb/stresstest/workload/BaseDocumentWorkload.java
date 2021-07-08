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
package com.arcadedb.stresstest.workload;

import com.arcadedb.database.Database;
import com.arcadedb.stresstest.DatabaseIdentifier;

/**
 * CRUD implementation of the workload.
 *
 * @author Luca Garulli (l.garulli--(at)--orientdb.com)
 */
public abstract class BaseDocumentWorkload extends OBaseWorkload {
  public class OWorkLoadContext extends OBaseWorkLoadContext {
    private Database db;

    @Override
    public void init(final DatabaseIdentifier dbIdentifier, int operationsPerTransaction) {
      db = dbIdentifier.getEmbeddedDatabase();
    }

    @Override
    public void close() {
      if (getDb() != null)
        getDb().close();
    }

    public Database getDb() {
      return db;
    }
  }

  @Override
  protected OBaseWorkLoadContext getContext() {
    return new OWorkLoadContext();
  }

  @Override
  protected void beginTransaction(final OBaseWorkLoadContext context) {
    ((OWorkLoadContext) context).db.begin();
  }

  @Override
  protected void commitTransaction(final OBaseWorkLoadContext context) {
    ((OWorkLoadContext) context).db.commit();
  }
}
