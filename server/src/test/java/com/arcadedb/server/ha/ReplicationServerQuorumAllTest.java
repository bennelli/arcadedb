/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb.server.ha;

import com.arcadedb.GlobalConfiguration;

public class ReplicationServerQuorumAllTest extends ReplicationServerTest {
  public ReplicationServerQuorumAllTest() {
    GlobalConfiguration.HA_QUORUM.setValue("ALL");
  }
}