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

/* Generated By:JJTree: Do not edit this line. ORecordAttribute.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultInternal;

import java.util.Map;

public class RecordAttribute extends SimpleNode {

  protected String name;

  public RecordAttribute(int id) {
    super(id);
  }

  public RecordAttribute(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    builder.append(name);
  }

  public RecordAttribute copy() {
    RecordAttribute result = new RecordAttribute(-1);
    result.name = name;
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    RecordAttribute that = (RecordAttribute) o;

    return name != null ? name.equals(that.name) : that.name == null;
  }

  @Override
  public int hashCode() {
    return name != null ? name.hashCode() : 0;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Result serialize() {
    ResultInternal result = new ResultInternal();
    result.setProperty("name", name);
    return result;
  }

  public void deserialize(Result fromResult) {
    name = fromResult.getProperty("name");
  }

  public Object evaluate(Result iCurrentRecord, CommandContext ctx) {
    if (name.equalsIgnoreCase("@rid")) {
      return iCurrentRecord.getIdentity().orElse(null);
    } else if (name.equalsIgnoreCase("@class")) {
      return iCurrentRecord.getElement().map(r -> r.getType()).orElse(null);
    }
//    else if (name.equalsIgnoreCase("@version")) {
//      return iCurrentRecord.getRecord().map(r -> r.getVersion()).orElse(null);
//    }
    return null;
  }
}
/* JavaCC - OriginalChecksum=45ce3cd16399dec7d7ef89f8920d02ae (do not edit this line) */
