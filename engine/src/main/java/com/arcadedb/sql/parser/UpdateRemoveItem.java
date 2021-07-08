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

/* Generated By:JJTree: Do not edit this line. OUpdateRemoveItem.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import com.arcadedb.sql.executor.CommandContext;
import com.arcadedb.sql.executor.MultiValue;
import com.arcadedb.sql.executor.ResultInternal;

import java.util.Map;

public class UpdateRemoveItem extends SimpleNode {

  Expression left;
  Expression right;

  public UpdateRemoveItem(int id) {
    super(id);
  }

  public UpdateRemoveItem(SqlParser p, int id) {
    super(p, id);
  }

  /**
   * Accept the visitor.
   **/
  public Object jjtAccept(SqlParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  public void toString(Map<Object, Object> params, StringBuilder builder) {
    left.toString(params, builder);
    if (right != null) {
      builder.append(" = ");
      right.toString(params, builder);
    }
  }

  public UpdateRemoveItem copy() {
    UpdateRemoveItem result = new UpdateRemoveItem(-1);
    result.left = left == null ? null : left.copy();
    result.right = right == null ? null : right.copy();
    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    UpdateRemoveItem that = (UpdateRemoveItem) o;

    if (left != null ? !left.equals(that.left) : that.left != null)
      return false;
    return right != null ? right.equals(that.right) : that.right == null;
  }

  @Override
  public int hashCode() {
    int result = left != null ? left.hashCode() : 0;
    result = 31 * result + (right != null ? right.hashCode() : 0);
    return result;
  }

  public void applyUpdate(ResultInternal result, CommandContext ctx) {
    if (right != null) {
      Object leftVal = left.execute(result, ctx);
      Object rightVal = right.execute(result, ctx);
      if (MultiValue.isMultiValue(leftVal)) {
        MultiValue.remove(leftVal, rightVal, false);
      }
    } else {
      left.applyRemove(result, ctx);
    }
  }
}
/* JavaCC - OriginalChecksum=72e240d3dc1196fdea69e8fdc2bd69ca (do not edit this line) */
