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

public class ReturnStatementTest extends ParserTestAbstract {

  @Test
  public void testPlain() {
    checkRightSyntax("RETURN");
    checkRightSyntax("return");
    checkRightSyntax("RETURN 1");
    checkRightSyntax("RETURN [1, 3]");
    checkRightSyntax("RETURN [$a, $b, $c]");
    checkRightSyntax("RETURN (select from foo)");

    checkWrongSyntax("return foo bar");
  }
}
