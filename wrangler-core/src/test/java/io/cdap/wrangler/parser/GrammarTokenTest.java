/*
 * Copyright © 2025 CDAP
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for grammar tokens related to byte size and time duration
 */
public class GrammarTokenTest {

  /**
   * Test parsing directives with BYTE_SIZE tokens
   */
  @Test
  public void testByteSizeToken() {
    String[] testRecipes = {
      "test-byte-size 10B;",
      "test-byte-size 1.5KB;",
      "test-byte-size 2MB;",
      "test-byte-size 3.5GB;",
      "test-byte-size 4TB;",
      "test-byte-size 5PB;"
    };

    for (String recipe : testRecipes) {
      DirectivesLexer lexer = new DirectivesLexer(new ANTLRInputStream(recipe));
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      DirectivesParser parser = new DirectivesParser(tokens);
      ParseTree tree = parser.recipe();
      
      Assert.assertEquals("Recipe should parse without errors: " + recipe, 
                         0, parser.getNumberOfSyntaxErrors());
      Assert.assertNotNull("Parse tree should be created: " + recipe, tree);
      
      // Check if we have BYTE_SIZE tokens in the token stream
      boolean foundByteSize = false;
      for (int i = 0; i < tokens.size(); i++) {
        if (tokens.get(i).getType() == DirectivesLexer.BYTE_SIZE) {
          foundByteSize = true;
          break;
        }
      }
      
      Assert.assertTrue("Should find BYTE_SIZE token in recipe: " + recipe, foundByteSize);
    }
  }

  /**
   * Test parsing directives with TIME_DURATION tokens
   */
  @Test
  public void testTimeDurationToken() {
    String[] testRecipes = {
      "test-time-duration 10ns;",
      "test-time-duration 20us;",
      "test-time-duration 30ms;",
      "test-time-duration 5s;",
      "test-time-duration 10m;",
      "test-time-duration 2h;",
      "test-time-duration 1d;"
    };

    for (String recipe : testRecipes) {
      DirectivesLexer lexer = new DirectivesLexer(new ANTLRInputStream(recipe));
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      DirectivesParser parser = new DirectivesParser(tokens);
      ParseTree tree = parser.recipe();
      
      Assert.assertEquals("Recipe should parse without errors: " + recipe, 
                         0, parser.getNumberOfSyntaxErrors());
      Assert.assertNotNull("Parse tree should be created: " + recipe, tree);
      
      // Check if we have TIME_DURATION tokens in the token stream
      boolean foundTimeDuration = false;
      for (int i = 0; i < tokens.size(); i++) {
        if (tokens.get(i).getType() == DirectivesLexer.TIME_DURATION) {
          foundTimeDuration = true;
          break;
        }
      }
      
      Assert.assertTrue("Should find TIME_DURATION token in recipe: " + recipe, foundTimeDuration);
    }
  }
} 

