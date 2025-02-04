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
package com.arcadedb.query.sql.parser;

import org.junit.jupiter.api.Test;

public class CreateBucketStatementTest extends ParserTestAbstract {

  @Test
  public void testPlain() {
    checkRightSyntax("CREATE BUCKET Foo");
    checkRightSyntax("CREATE BUCKET Foo ID 14");
    checkRightSyntax("create bucket Foo");
    checkRightSyntax("create bucket Foo id 14");
    checkRightSyntax("CREATE BLOB BUCKET Foo");
    checkRightSyntax("create blob bucket Foo id 14");

    checkRightSyntax("create blob bucket Foo IF NOT EXISTS");
    checkRightSyntax("create blob bucket Foo IF NOT EXISTS id 14");

    checkWrongSyntax("CREATE Bucket");
    checkWrongSyntax("CREATE Bucket foo bar");
    checkWrongSyntax("CREATE Bucket foo.bar");
    checkWrongSyntax("CREATE Bucket foo id bar");

    checkWrongSyntax("create blob bucket Foo IF EXISTS");
    checkWrongSyntax("create blob bucket Foo IF EXISTS id 14");
  }
}
