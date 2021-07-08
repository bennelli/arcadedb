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

/* Generated By:JJTree: Do not edit this line. ORollbackStatement.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.InternalResultSet;
import com.arcadedb.sql.executor.ResultInternal;
import com.arcadedb.sql.executor.ResultSet;

import java.util.Map;

public class RollbackStatement extends SimpleExecStatement {
  public RollbackStatement(int id) {
    super(id);
  }

  public RollbackStatement(SqlParser p, int id) {
    super(p, id);
  }

  @Override public ResultSet executeSimple(CommandContext ctx) {
    ctx.getDatabase().rollback();
    InternalResultSet result = new InternalResultSet();
    ResultInternal item = new ResultInternal();
    item.setProperty("operation", "rollback");
    result.add(item);
    return result;
  }


  @Override public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append("ROLLBACK");
  }

  @Override public RollbackStatement copy() {
    RollbackStatement result = new RollbackStatement(-1);
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    return o != null && getClass() == o.getClass();
  }

  @Override public int hashCode() {
    return 0;
  }
}
/* JavaCC - OriginalChecksum=7efe0306e0cec51e035d64cad02ebc30 (do not edit this line) */
