/*
 * Copyright (c) 2018 - Arcade Analytics LTD (https://arcadeanalytics.com)
 */

package com.arcadedb;

import com.arcadedb.database.Database;
import com.arcadedb.database.Record;
import com.arcadedb.engine.DatabaseChecker;
import com.arcadedb.exception.RecordNotFoundException;
import com.arcadedb.graph.Edge;
import com.arcadedb.graph.MutableEdge;
import com.arcadedb.graph.MutableVertex;
import com.arcadedb.graph.Vertex;
import com.arcadedb.sql.executor.Result;
import com.arcadedb.sql.executor.ResultSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

public class GraphTest extends BaseGraphTest {
  @Test
  public void checkVertices() {
    database.begin();
    try {

      Assertions.assertEquals(1, database.countType(VERTEX1_TYPE_NAME, false));
      Assertions.assertEquals(2, database.countType(VERTEX2_TYPE_NAME, false));

      final Vertex v1 = (Vertex) database.lookupByRID(root, false);
      Assertions.assertNotNull(v1);

      // TEST CONNECTED VERTICES
      Assertions.assertEquals(VERTEX1_TYPE_NAME, v1.getType());
      Assertions.assertEquals(VERTEX1_TYPE_NAME, v1.get("name"));

      final Iterator<Vertex> vertices2level = v1.getVertices(Vertex.DIRECTION.OUT, new String[] { EDGE1_TYPE_NAME }).iterator();
      Assertions.assertNotNull(vertices2level);
      Assertions.assertTrue(vertices2level.hasNext());

      final Vertex v2 = vertices2level.next();

      Assertions.assertNotNull(v2);
      Assertions.assertEquals(VERTEX2_TYPE_NAME, v2.getType());
      Assertions.assertEquals(VERTEX2_TYPE_NAME, v2.get("name"));

      final Iterator<Vertex> vertices2level2 = v1.getVertices(Vertex.DIRECTION.OUT, new String[] { EDGE2_TYPE_NAME }).iterator();
      Assertions.assertTrue(vertices2level2.hasNext());

      final Vertex v3 = vertices2level2.next();
      Assertions.assertNotNull(v3);

      Assertions.assertEquals(VERTEX2_TYPE_NAME, v3.getType());
      Assertions.assertEquals("V3", v3.get("name"));

      final Iterator<Vertex> vertices3level = v2.getVertices(Vertex.DIRECTION.OUT, new String[] { EDGE2_TYPE_NAME }).iterator();
      Assertions.assertNotNull(vertices3level);
      Assertions.assertTrue(vertices3level.hasNext());

      final Vertex v32 = vertices3level.next();

      Assertions.assertNotNull(v32);
      Assertions.assertEquals(VERTEX2_TYPE_NAME, v32.getType());
      Assertions.assertEquals("V3", v32.get("name"));

      Assertions.assertTrue(v1.isConnectedTo(v2));
      Assertions.assertTrue(v2.isConnectedTo(v1));
      Assertions.assertTrue(v1.isConnectedTo(v3));
      Assertions.assertTrue(v3.isConnectedTo(v1));
      Assertions.assertTrue(v2.isConnectedTo(v3));

      Assertions.assertFalse(v3.isConnectedTo(v1, Vertex.DIRECTION.OUT));
      Assertions.assertFalse(v3.isConnectedTo(v2, Vertex.DIRECTION.OUT));

    } finally {
      database.commit();
    }
  }

  @Test
  //TODO
  public void autoPersistLightWeightEdge() {
    database.begin();
    try {
      final Vertex v1 = (Vertex) database.lookupByRID(root, false);
      Assertions.assertNotNull(v1);

      final Iterator<Edge> edges3 = v1.getEdges(Vertex.DIRECTION.OUT, new String[] { EDGE2_TYPE_NAME }).iterator();
      Assertions.assertNotNull(edges3);
      Assertions.assertTrue(edges3.hasNext());

      try {
        final MutableEdge edge = edges3.next().modify();
        Assertions.fail("Cannot modify lightweight edges");
//        edge.set("upgraded", true);
//        edge.save();
//
//        Assertions.assertTrue(edge.getIdentity().getPosition() > -1);
      } catch (IllegalStateException e) {
      }

    } finally {
      database.commit();
    }
  }

  @Test
  public void checkEdges() {
    database.begin();
    try {

      Assertions.assertEquals(1, database.countType(EDGE1_TYPE_NAME, false));
      Assertions.assertEquals(1, database.countType(EDGE2_TYPE_NAME, false));

      final Vertex v1 = (Vertex) database.lookupByRID(root, false);
      Assertions.assertNotNull(v1);

      // TEST CONNECTED EDGES
      final Iterator<Edge> edges1 = v1.getEdges(Vertex.DIRECTION.OUT, new String[] { EDGE1_TYPE_NAME }).iterator();
      Assertions.assertNotNull(edges1);
      Assertions.assertTrue(edges1.hasNext());

      final Edge e1 = edges1.next();

      Assertions.assertNotNull(e1);
      Assertions.assertEquals(EDGE1_TYPE_NAME, e1.getType());
      Assertions.assertEquals(v1, e1.getOut());
      Assertions.assertEquals("E1", e1.get("name"));

      Vertex v2 = e1.getInVertex();
      Assertions.assertEquals(VERTEX2_TYPE_NAME, v2.get("name"));

      final Iterator<Edge> edges2 = v2.getEdges(Vertex.DIRECTION.OUT, new String[] { EDGE2_TYPE_NAME }).iterator();
      Assertions.assertTrue(edges2.hasNext());

      final Edge e2 = edges2.next();
      Assertions.assertNotNull(e2);

      Assertions.assertEquals(EDGE2_TYPE_NAME, e2.getType());
      Assertions.assertEquals(v2, e2.getOut());
      Assertions.assertEquals("E2", e2.get("name"));

      Vertex v3 = e2.getInVertex();
      Assertions.assertEquals("V3", v3.get("name"));

      final Iterator<Edge> edges3 = v1.getEdges(Vertex.DIRECTION.OUT, new String[] { EDGE2_TYPE_NAME }).iterator();
      Assertions.assertNotNull(edges3);
      Assertions.assertTrue(edges3.hasNext());

      final Edge e3 = edges3.next();

      Assertions.assertNotNull(e3);
      Assertions.assertEquals(EDGE2_TYPE_NAME, e3.getType());
      Assertions.assertEquals(v1, e3.getOutVertex());
      Assertions.assertEquals(v3, e3.getInVertex());

      v2.getEdges();

    } finally {
      database.commit();
    }
  }

  @Test
  public void updateVerticesAndEdges() {
    database.begin();
    try {

      Assertions.assertEquals(1, database.countType(EDGE1_TYPE_NAME, false));
      Assertions.assertEquals(1, database.countType(EDGE2_TYPE_NAME, false));

      final Vertex v1 = (Vertex) database.lookupByRID(root, false);
      Assertions.assertNotNull(v1);

      final MutableVertex v1Copy = (MutableVertex) v1.modify();
      v1Copy.set("newProperty1", "TestUpdate1");
      v1Copy.save();

      // TEST CONNECTED EDGES
      final Iterator<Edge> edges1 = v1.getEdges(Vertex.DIRECTION.OUT, new String[] { EDGE1_TYPE_NAME }).iterator();
      Assertions.assertNotNull(edges1);
      Assertions.assertTrue(edges1.hasNext());

      final Edge e1 = edges1.next();

      Assertions.assertNotNull(e1);

      final MutableEdge e1Copy = (MutableEdge) e1.modify();
      e1Copy.set("newProperty2", "TestUpdate2");
      e1Copy.save();

      database.commit();

      final Vertex v1CopyReloaded = (Vertex) database.lookupByRID(v1Copy.getIdentity(), true);
      Assertions.assertEquals("TestUpdate1", v1CopyReloaded.get("newProperty1"));
      final Edge e1CopyReloaded = (Edge) database.lookupByRID(e1Copy.getIdentity(), true);
      Assertions.assertEquals("TestUpdate2", e1CopyReloaded.get("newProperty2"));

    } finally {
      new DatabaseChecker().check(database);
    }
  }

  @Test
  public void deleteVertices() {
    database.begin();
    try {

      Vertex v1 = (Vertex) database.lookupByRID(root, false);
      Assertions.assertNotNull(v1);

      Iterator<Vertex> vertices = v1.getVertices(Vertex.DIRECTION.OUT).iterator();
      Assertions.assertTrue(vertices.hasNext());
      Vertex v2 = vertices.next();
      Assertions.assertNotNull(v2);

      Assertions.assertTrue(vertices.hasNext());
      Vertex v3 = vertices.next();
      Assertions.assertNotNull(v3);

      final long totalVertices = database.countType(v1.getType(), true);

      // DELETE THE VERTEX
      // -----------------------
      database.deleteRecord(v1);

      Assertions.assertEquals(totalVertices - 1, database.countType(v1.getType(), true));

      vertices = v2.getVertices(Vertex.DIRECTION.IN).iterator();
      Assertions.assertFalse(vertices.hasNext());

      vertices = v2.getVertices(Vertex.DIRECTION.OUT).iterator();
      Assertions.assertTrue(vertices.hasNext());

      // Expecting 1 edge only: V2 is still connected to V3
      vertices = v3.getVertices(Vertex.DIRECTION.IN).iterator();
      Assertions.assertTrue(vertices.hasNext());
      vertices.next();
      Assertions.assertFalse(vertices.hasNext());

      // RELOAD AND CHECK AGAIN
      // -----------------------
      v2 = (Vertex) database.lookupByRID(v2.getIdentity(), true);

      vertices = v2.getVertices(Vertex.DIRECTION.IN).iterator();
      Assertions.assertFalse(vertices.hasNext());

      vertices = v2.getVertices(Vertex.DIRECTION.OUT).iterator();
      Assertions.assertTrue(vertices.hasNext());

      v3 = (Vertex) database.lookupByRID(v3.getIdentity(), true);

      // Expecting 1 edge only: V2 is still connected to V3
      vertices = v3.getVertices(Vertex.DIRECTION.IN).iterator();
      Assertions.assertTrue(vertices.hasNext());
      vertices.next();
      Assertions.assertFalse(vertices.hasNext());

      try {
        database.lookupByRID(root, true);
        Assertions.fail("Expected deleted record");
      } catch (RecordNotFoundException e) {
      }

    } finally {
      database.commit();
      new DatabaseChecker().check(database);
    }
  }

  @Test
  public void deleteEdges() {
    final Database db = database;
    database.begin();
    try {

      Vertex v1 = (Vertex) database.lookupByRID(root, false);
      Assertions.assertNotNull(v1);

      Iterator<Edge> edges = v1.getEdges(Vertex.DIRECTION.OUT).iterator();
      Assertions.assertTrue(edges.hasNext());
      Edge e2 = edges.next();
      Assertions.assertNotNull(e2);

      Assertions.assertTrue(edges.hasNext());
      Edge e3 = edges.next();
      Assertions.assertNotNull(e3);

      // DELETE THE EDGE
      // -----------------------
      database.deleteRecord(e2);

      Vertex vOut = e2.getOutVertex();
      edges = vOut.getEdges(Vertex.DIRECTION.OUT).iterator();
      Assertions.assertTrue(edges.hasNext());

      edges.next();
      Assertions.assertFalse(edges.hasNext());

      Vertex vIn = e2.getInVertex();
      edges = vIn.getEdges(Vertex.DIRECTION.IN).iterator();
      Assertions.assertFalse(edges.hasNext());

      // RELOAD AND CHECK AGAIN
      // -----------------------
      try {
        database.lookupByRID(e2.getIdentity(), true);
        Assertions.fail("Expected deleted record");
      } catch (RecordNotFoundException e) {
      }

      vOut = e2.getOutVertex();
      edges = vOut.getEdges(Vertex.DIRECTION.OUT).iterator();
      Assertions.assertTrue(edges.hasNext());

      edges.next();
      Assertions.assertFalse(edges.hasNext());

      vIn = e2.getInVertex();
      edges = vIn.getEdges(Vertex.DIRECTION.IN).iterator();
      Assertions.assertFalse(edges.hasNext());

    } finally {
      database.commit();
      new DatabaseChecker().check(database);
    }
  }

  @Test
  public void selfLoopEdges() {
    database.begin();
    try {

      // UNIDIRECTIONAL EDGE
      final Vertex v1 = database.newVertex(VERTEX1_TYPE_NAME).save();
      v1.newEdge(EDGE1_TYPE_NAME, v1, false).save();

      Assertions.assertTrue(v1.getVertices(Vertex.DIRECTION.OUT).iterator().hasNext());
      Assertions.assertEquals(v1, v1.getVertices(Vertex.DIRECTION.OUT).iterator().next());
      Assertions.assertFalse(v1.getVertices(Vertex.DIRECTION.IN).iterator().hasNext());

      // BIDIRECTIONAL EDGE
      final Vertex v2 = database.newVertex(VERTEX1_TYPE_NAME).save();
      v2.newEdge(EDGE1_TYPE_NAME, v2, true).save();

      Assertions.assertTrue(v2.getVertices(Vertex.DIRECTION.OUT).iterator().hasNext());
      Assertions.assertEquals(v2, v2.getVertices(Vertex.DIRECTION.OUT).iterator().next());

      Assertions.assertTrue(v2.getVertices(Vertex.DIRECTION.IN).iterator().hasNext());
      Assertions.assertEquals(v2, v2.getVertices(Vertex.DIRECTION.IN).iterator().next());

      database.commit();

      // UNIDIRECTIONAL EDGE
      final Vertex v1reloaded = (Vertex) database.lookupByRID(v1.getIdentity(), true);
      Assertions.assertTrue(v1reloaded.getVertices(Vertex.DIRECTION.OUT).iterator().hasNext());
      Assertions.assertEquals(v1reloaded, v1reloaded.getVertices(Vertex.DIRECTION.OUT).iterator().next());
      Assertions.assertFalse(v1reloaded.getVertices(Vertex.DIRECTION.IN).iterator().hasNext());

      // BIDIRECTIONAL EDGE
      final Vertex v2reloaded = (Vertex) database.lookupByRID(v2.getIdentity(), true);

      Assertions.assertTrue(v2reloaded.getVertices(Vertex.DIRECTION.OUT).iterator().hasNext());
      Assertions.assertEquals(v2reloaded, v2reloaded.getVertices(Vertex.DIRECTION.OUT).iterator().next());

      Assertions.assertTrue(v2reloaded.getVertices(Vertex.DIRECTION.IN).iterator().hasNext());
      Assertions.assertEquals(v2reloaded, v2reloaded.getVertices(Vertex.DIRECTION.IN).iterator().next());

    } finally {
      new DatabaseChecker().check(database);
    }
  }

  @Test
  public void shortestPath() {
    database.begin();
    try {

      final Iterator<Record> v1Iterator = database.iterateType(VERTEX1_TYPE_NAME, true);
      while (v1Iterator.hasNext()) {

        final Record v1 = v1Iterator.next();

        final Iterator<Record> v2Iterator = database.iterateType(VERTEX2_TYPE_NAME, true);
        while (v2Iterator.hasNext()) {

          final Record v2 = v2Iterator.next();

          final ResultSet result = database.query("sql", "select shortestPath(?,?) as sp", v1, v2);
          Assertions.assertTrue(result.hasNext());
          Result line = result.next();

          Assertions.assertNotNull(line);
          Assertions.assertTrue(line.getPropertyNames().contains("sp"));
          Assertions.assertNotNull(line.getProperty("sp"));
          Assertions.assertEquals(2, ((List) line.getProperty("sp")).size());
          Assertions.assertEquals(v1, ((List) line.getProperty("sp")).get(0));
          Assertions.assertEquals(v2, ((List) line.getProperty("sp")).get(1));
        }
      }

    } finally {
      new DatabaseChecker().check(database);
    }
  }
}