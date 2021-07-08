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

/* Generated By:JJTree: Do not edit this line. OUpdateIncrementItem.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=true,NODE_PREFIX=O,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_USERTYPE_VISIBILITY_PUBLIC=true */
package com.arcadedb.sql.parser;

import java.util.Map;

public class UpdateIncrementItem extends SimpleNode {
  protected Identifier left;
  protected Modifier   leftModifier;
  protected Expression right;

  public UpdateIncrementItem(int id) {
    super(id);
  }

  public UpdateIncrementItem(SqlParser p, int id) {
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
    if (leftModifier != null) {
      leftModifier.toString(params, builder);
    }
    builder.append(" = ");
    right.toString(params, builder);
  }

  public UpdateIncrementItem copy() {
    UpdateIncrementItem result = new UpdateIncrementItem(-1);
    result.left = left == null ? null : left.copy();
    result.leftModifier = leftModifier == null ? null : leftModifier.copy();
    result.right = right == null ? null : right.copy();
    return result;
  }

  @Override public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    UpdateIncrementItem that = (UpdateIncrementItem) o;

    if (left != null ? !left.equals(that.left) : that.left != null)
      return false;
    if (leftModifier != null ? !leftModifier.equals(that.leftModifier) : that.leftModifier != null)
      return false;
    return right != null ? right.equals(that.right) : that.right == null;
  }

  @Override public int hashCode() {
    int result = left != null ? left.hashCode() : 0;
    result = 31 * result + (leftModifier != null ? leftModifier.hashCode() : 0);
    result = 31 * result + (right != null ? right.hashCode() : 0);
    return result;
  }
}
/* JavaCC - OriginalChecksum=94dd82febb904e4e31130bdcbbb48fe3 (do not edit this line) */
