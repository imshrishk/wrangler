/*
 * Copyright © 2023 CDAP
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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

/**
 * A demonstration class for parsing directives and using the custom visitor.
 */
public class DirectiveParserDemo {

  /**
   * Main method for running the demo.
   */
  public static void main(String[] args) {
    // Example recipe containing various directives
    String recipe = String.join("\n",
      "# This is a sample recipe with multiple directives",
      "parse-as-csv ',' true;",
      "drop :col1,:col2,:col3;",
      "rename :originalName :newName;",
      "set-column :qualified prop:{type='string',nullable=false,format='yyyy-MM-dd'};",
      "find-and-replace :col1 'find' 'replace';",
      "split-to-rows :notes ' ';",
      "# This is a more complex directive with a condition",
      "filter-row exp:{ !isempty(:id) && isdate(:date, 'yyyy-MM-dd') };"
    );

    try {
      // Parse the recipe and extract directives
      List<MyRecipeVisitor.DirectiveInfo> directives = parseRecipe(recipe);
      
      // Print all the extracted directives
      System.out.println("Found " + directives.size() + " directives:");
      for (MyRecipeVisitor.DirectiveInfo directive : directives) {
        System.out.println("\nLine " + directive.getLineNumber() + ": " + directive.getName());
        System.out.println("Raw text: " + directive.getRawText());
        System.out.println("Arguments: " + directive.getArguments());
      }
    } catch (Exception e) {
      System.err.println("Error parsing recipe: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Parse a recipe string and return information about the directives.
   *
   * @param recipeStr The recipe string to parse
   * @return A list of DirectiveInfo objects
   */
  public static List<MyRecipeVisitor.DirectiveInfo> parseRecipe(String recipeStr) {
    // Create an input stream from the recipe string
    CharStream input = CharStreams.fromString(recipeStr);
    
    // Create a lexer to tokenize the input
    DirectivesLexer lexer = new DirectivesLexer(input);
    
    // Create a token stream from the lexer
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    
    // Create a parser to process the tokens
    DirectivesParser parser = new DirectivesParser(tokens);
    
    // Use our custom error listener to capture and report syntax errors
    SyntaxErrorListener errorListener = new SyntaxErrorListener();
    parser.removeErrorListeners();
    parser.addErrorListener(errorListener);
    
    // Parse the recipe
    ParseTree tree = parser.recipe();
    
    // Create our custom visitor and visit the parse tree
    MyRecipeVisitor visitor = new MyRecipeVisitor();
    visitor.visit(tree);
    
    // Return the collected directive information
    return visitor.getDirectiveInfoList();
  }
}
