/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */
package com.arcadedb.stresstest.workload;

import com.arcadedb.stresstest.DatabaseIdentifier;

/**
 * Supports checking of the workload.
 *
 * @author Luca Garulli (l.garulli--(at)--orientdb.com)
 */
public interface CheckWorkload {
  void check(DatabaseIdentifier databaseIdentifier);
}