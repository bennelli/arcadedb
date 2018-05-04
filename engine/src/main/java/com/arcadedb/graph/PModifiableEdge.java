package com.arcadedb.graph;

import com.arcadedb.database.PBinary;
import com.arcadedb.database.PDatabase;
import com.arcadedb.database.PModifiableDocument;
import com.arcadedb.database.PRID;

public class PModifiableEdge extends PModifiableDocument implements PEdge {
  private PRID out;
  private PRID in;

  public PModifiableEdge(final PDatabase graph, final String typeName, final PRID out, PRID in) {
    super(graph, typeName, null);
    this.out = out;
    this.in = in;
  }

  public PModifiableEdge(final PDatabase graph, final String typeName, final PRID rid) {
    super(graph, typeName, rid);
  }

  public PModifiableEdge(final PDatabase graph, final String typeName, final PRID rid, final PBinary buffer) {
    super(graph, typeName, rid, buffer);
    this.out = new PRID(graph, buffer.getInt(), buffer.getLong());
    this.in = new PRID(graph, buffer.getInt(), buffer.getLong());
    this.propertiesStartingPosition = buffer.position();
  }

  @Override
  public PRID getOut() {
    return out;
  }

  @Override
  public PVertex getOutVertex() {
    return (PVertex) out.getRecord();
  }

  @Override
  public PRID getIn() {
    return in;
  }

  @Override
  public PVertex getInVertex() {
    return (PVertex) in.getRecord();
  }

  @Override
  public byte getRecordType() {
    return PEdge.RECORD_TYPE;
  }
}