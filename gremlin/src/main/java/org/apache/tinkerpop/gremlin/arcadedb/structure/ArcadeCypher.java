/*
 * Copyright © 2021-present Arcade Data Ltd (info@arcadedata.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tinkerpop.gremlin.arcadedb.structure;

import com.arcadedb.GlobalConfiguration;
import org.opencypher.gremlin.translation.TranslationFacade;

import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * Cypher Expression builder. Transform a cypher expression into Gremlin.
 *
 * @author Luca Garulli (l.garulli@arcadedata.com)
 */

public class ArcadeCypher extends ArcadeGremlin {
  private static final HashMap<String, CachedStatement> STATEMENT_CACHE       = new HashMap<>();
  private static final int                              CACHE_SIZE            = GlobalConfiguration.CYPHER_STATEMENT_CACHE.getValueAsInteger();
  private static       AtomicInteger                    totalCachedStatements = new AtomicInteger(0);

  private static class CachedStatement {
    public final String cypher;
    public final String gremlin;
    public       int    used = 0;

    private CachedStatement(final String cypher, final String gremlin) {
      this.cypher = cypher;
      this.gremlin = gremlin;
    }
  }

  protected ArcadeCypher(final ArcadeGraph graph, final String cypherQuery) {
    super(graph, compileToGremlin(graph, cypherQuery));
  }

  public static String compileToGremlin(final ArcadeGraph graph, final String cypher) {
    if (CACHE_SIZE == 0)
      // NO CACHE
      return new TranslationFacade().toGremlinGroovy(cypher);

    synchronized (STATEMENT_CACHE) {
      final String db = graph.getDatabase().getDatabasePath();

      final String mapKey = db + ":" + cypher;

      CachedStatement cached = STATEMENT_CACHE.get(mapKey);
      // FOUND
      if (cached != null) {
        ++cached.used;
        return cached.gremlin;
      }

      // TRANSLATE TO GREMLIN AND CACHE THE STATEMENT FOR FURTHER USAGE
      final String gremlin = new TranslationFacade().toGremlinGroovy(cypher);

      while (totalCachedStatements.get() >= CACHE_SIZE) {
        int leastUsedValue = 0;
        String leastUsedKey = null;

        for (Map.Entry<String, CachedStatement> entry : STATEMENT_CACHE.entrySet()) {
          if (leastUsedKey == null || entry.getValue().used < leastUsedValue) {
            leastUsedKey = entry.getKey();
            leastUsedValue = entry.getValue().used;
          }
        }

        STATEMENT_CACHE.remove(leastUsedKey);
        STATEMENT_CACHE.put(cypher, new CachedStatement(cypher, gremlin));
      }

      return gremlin;
    }
  }

  public static void closeDatabase(final ArcadeGraph graph) {
    synchronized (STATEMENT_CACHE) {
      final String mapKey = graph.getDatabase().getDatabasePath() + ":";

      // REMOVE ALL THE ENTRIES RELATIVE TO THE CLOSED DATABASE
      for (Iterator<Map.Entry<String, CachedStatement>> it = STATEMENT_CACHE.entrySet().iterator(); it.hasNext(); ) {
        if (it.next().getKey().startsWith(mapKey))
          it.remove();
      }
    }
  }
}
