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

public class DefaultBucketSelectionStrategy implements BucketSelectionStrategy {
  private volatile int current = -1;
  private          int total;

  @Override
  public void setTotalBuckets(final int total) {
    this.total = total;
    if (current >= total)
      // RESET IT
      current = -1;
  }

  @Override
  public int getBucketToSave(final boolean async) {
    if (async)
      return (int) (Thread.currentThread().getId() % total);

    // COPY THE VALUE ON THE HEAP FOR MULTI-THREAD ACCESS
    int bucketIndex = ++current;
    if (bucketIndex >= total) {
      current = 0;
      bucketIndex = 0;
    }
    return bucketIndex;
  }

  @Override
  public String getName() {
    return "round-robin";
  }

  @Override
  public String toString() {
    return getName();
  }
}
