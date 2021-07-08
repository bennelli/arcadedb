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

package com.arcadedb.sql.executor;

import com.arcadedb.sql.parser.Rid;
import com.arcadedb.sql.parser.WhereClause;

/**
 * Created by luigidellaquila on 20/09/16.
 */
public class EdgeTraversal {
  boolean out = true;
  public  PatternEdge edge;
  private String      leftClass;
  private String      leftCluster;
  private Rid         leftRid;
  private WhereClause leftFilter;

  public EdgeTraversal(PatternEdge edge, boolean out) {
    this.edge = edge;
    this.out = out;
  }

  public void setLeftClass(String leftClass) {
    this.leftClass = leftClass;
  }

  public void setLeftFilter(WhereClause leftFilter) {
    this.leftFilter = leftFilter;
  }

  public String getLeftClass() {
    return leftClass;
  }
  public String getLeftCluster() {
    return leftCluster;
  }

  public Rid getLeftRid() {
    return leftRid;
  }

  public void setLeftCluster(String leftCluster) {
    this.leftCluster = leftCluster;
  }

  public void setLeftRid(Rid leftRid) {
    this.leftRid = leftRid;
  }

  public WhereClause getLeftFilter() {
    return leftFilter;
  }

  @Override
  public String toString() {
    return edge.toString();
  }
}