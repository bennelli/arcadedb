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

package com.arcadedb.mongodbw;

import com.arcadedb.GlobalConfiguration;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import org.bson.BsonDocument;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.mongodb.client.model.Filters.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MongoDBWTest extends BaseGraphServerTest {

  private static final int                                                   DEF_PORT = 27017;
  private              com.mongodb.client.MongoCollection<org.bson.Document> collection;
  private              MongoClient                                           client;

  @BeforeEach
  @Override
  public void beginTest() {
    GlobalConfiguration.SERVER_PLUGINS.setValue("MongoDB:com.arcadedb.mongodbw.MongoDBWrapperPlugin");
    super.beginTest();
  }

  @AfterEach
  @Override
  public void endTest() {
    GlobalConfiguration.SERVER_PLUGINS.setValue("");
    client.close();
    super.endTest();
  }

  @Test
  public void testSimpleInsertQuery() throws Exception {
    client = new MongoClient(new ServerAddress("localhost", DEF_PORT));
    client.getDatabase(getDatabaseName()).createCollection("MongoDBCollection");

    collection = client.getDatabase(getDatabaseName()).getCollection("MongoDBCollection");
    assertEquals(0, collection.count());

    Document obj = new Document("id", 0).append("name", "Jay").append("lastName", "Miner");
    collection.insertOne(obj);

    assertEquals(1, collection.count());

    assertEquals(obj, collection.find().first());

    assertEquals(obj, collection.find(BsonDocument.parse("{ name: \"Jay\" } ")).first());

    assertNull(collection.find(BsonDocument.parse("{ name: \"Jay2\" } ")).first());

    assertEquals(obj, collection.find(BsonDocument.parse("{ name: { $eq: \"Jay\" } } ")).first());

    assertEquals(obj, collection.find(BsonDocument.parse("{ name: { $ne: \"Jay2\" } } ")).first());

    assertEquals(obj, collection.find(BsonDocument.parse("{ name: { $in: [ \"Jay\", \"John\" ] } } ")).first());

    assertEquals(obj, collection.find(BsonDocument.parse("{ name: { $nin: [ \"Jay2\", \"John\" ] } } ")).first());

    assertEquals(obj, collection.find(BsonDocument.parse("{ name: { $lt: \"Jay2\" } } ")).first());

    assertEquals(obj, collection.find(BsonDocument.parse("{ name: { $lte: \"Jay2\" } } ")).first());

    assertEquals(obj, collection.find(BsonDocument.parse("{ name: { $gt: \"A\" } } ")).first());

    assertEquals(obj, collection.find(BsonDocument.parse("{ name: { $gte: \"A\" } } ")).first());

    assertEquals(obj, collection.find(and(gt("name", "A"), lte("name", "Jay"))).first());

    assertEquals(obj,
        collection.find(BsonDocument.parse("{ $or: [ { name: { $eq: 'Jay' } }, { lastName: 'Miner222'} ] }")).first());

    assertEquals(obj, collection.find(BsonDocument.parse("{ $not: { name: { $eq: 'Jay2' } } }")).first());

    assertEquals(obj, collection.find(BsonDocument.parse(
        "{ $and: [ { name: { $eq: 'Jay' } }, { lastName: { $exists: true } }, { lastName: { $eq: 'Miner' } }, { lastName: { $ne: 'Miner22' } } ] }"))
        .first());

    assertEquals(obj,
        collection.find(and(eq("name", "Jay"), exists("lastName"), eq("lastName", "Miner"), ne("lastName", "Miner22"))).first());

  }
}