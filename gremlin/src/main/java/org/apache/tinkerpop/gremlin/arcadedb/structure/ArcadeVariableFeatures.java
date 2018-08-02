/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package org.apache.tinkerpop.gremlin.arcadedb.structure;

import org.apache.tinkerpop.gremlin.structure.Graph;

public class ArcadeVariableFeatures implements Graph.Features.VariableFeatures {

  @Override
  public boolean supportsVariables() {
    return false;
  }

  @Override
  public boolean supportsBooleanArrayValues() {
    return false;
  }

  @Override
  public boolean supportsBooleanValues() {
    return false;
  }

  @Override
  public boolean supportsByteArrayValues() {
    return false;
  }

  @Override
  public boolean supportsByteValues() {
    return false;
  }

  @Override
  public boolean supportsDoubleArrayValues() {
    return false;
  }

  @Override
  public boolean supportsDoubleValues() {
    return false;
  }

  @Override
  public boolean supportsFloatArrayValues() {
    return false;
  }

  @Override
  public boolean supportsFloatValues() {
    return false;
  }

  @Override
  public boolean supportsIntegerArrayValues() {
    return false;
  }

  @Override
  public boolean supportsIntegerValues() {
    return false;
  }

  @Override
  public boolean supportsLongArrayValues() {
    return false;
  }

  @Override
  public boolean supportsLongValues() {
    return false;
  }

  @Override
  public boolean supportsMapValues() {
    return false;
  }

  @Override
  public boolean supportsMixedListValues() {
    return false;
  }

  @Override
  public boolean supportsSerializableValues() {
    return false;
  }

  @Override
  public boolean supportsStringArrayValues() {
    return false;
  }

  @Override
  public boolean supportsStringValues() {
    return false;
  }

  @Override
  public boolean supportsUniformListValues() {
    return false;
  }
}