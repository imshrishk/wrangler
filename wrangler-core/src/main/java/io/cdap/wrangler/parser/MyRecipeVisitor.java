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

import io.cdap.wrangler.api.SourceInfo;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implements a visitor pattern for parsing and collecting information
 * about directives in a recipe.
 */
public class MyRecipeVisitor extends DirectivesBaseVisitor<String> {
  
  private List<DirectiveInfo> directiveInfoList = new ArrayList<>();
  private DirectiveInfo currentDirective = null;
  private int directiveCount = 0;
  
  /**
   * Represents information about a directive in the recipe.
   */
  public static class DirectiveInfo {
    private String name;
    private Map<String, Object> arguments = new HashMap<>();
    private String rawText;
    private int lineNumber;
    
    public DirectiveInfo(String name, String rawText, int lineNumber) {
      this.name = name;
      this.rawText = rawText;
      this.lineNumber = lineNumber;
    }
    
    public void addArgument(String key, Object value) {
      arguments.put(key, value);
    }
    
    public String getName() {
      return name;
    }
    
    public Map<String, Object> getArguments() {
      return arguments;
    }
    
    public String getRawText() {
      return rawText;
    }
    
    public int getLineNumber() {
      return lineNumber;
    }
    
    @Override
    public String toString() {
      return "Directive: " + name + ", Arguments: " + arguments + ", Line: " + lineNumber;
    }
  }
  
  /**
   * Gets the list of directive information collected during parsing.
   *
   * @return List of DirectiveInfo objects.
   */
  public List<DirectiveInfo> getDirectiveInfoList() {
    return directiveInfoList;
  }
  
  /**
   * Called when a directive is visited.
   */
  @Override
  public String visitDirective(DirectivesParser.DirectiveContext ctx) {
    // Get the raw source text for this directive
    String originalSource = getOriginalSource(ctx);
    int lineNumber = ctx.getStart().getLine();
    
    directiveCount++;
    currentDirective = null;
    
    // Continue visiting child nodes
    return super.visitDirective(ctx);
  }
  
  /**
   * Called when a command (directive name) is visited.
   */
  @Override
  public String visitCommand(DirectivesParser.CommandContext ctx) {
    String directiveName = ctx.Identifier().getText();
    String originalSource = getOriginalSource(ctx.getParent());
    int lineNumber = ctx.getStart().getLine();
    
    currentDirective = new DirectiveInfo(directiveName, originalSource, lineNumber);
    directiveInfoList.add(currentDirective);
    
    return directiveName;
  }
  
  /**
   * Called when a column name is visited.
   */
  @Override
  public String visitColumn(DirectivesParser.ColumnContext ctx) {
    String columnName = ctx.Column().getText().substring(1); // Remove the leading ':'
    
    if (currentDirective != null) {
      currentDirective.addArgument("column", columnName);
    }
    
    return columnName;
  }
  
  /**
   * Called when a column list is visited.
   */
  @Override
  public String visitColList(DirectivesParser.ColListContext ctx) {
    List<String> columns = new ArrayList<>();
    
    for (TerminalNode column : ctx.Column()) {
      columns.add(column.getText().substring(1)); // Remove the leading ':'
    }
    
    if (currentDirective != null) {
      currentDirective.addArgument("columns", columns);
    }
    
    return String.join(",", columns);
  }
  
  /**
   * Called when a text (string) is visited.
   */
  @Override
  public String visitText(DirectivesParser.TextContext ctx) {
    String text = ctx.String().getText();
    // Remove surrounding quotes
    text = text.substring(1, text.length() - 1);
    
    if (currentDirective != null) {
      // If we already have a text argument, make it a list
      if (currentDirective.getArguments().containsKey("text")) {
        Object existing = currentDirective.getArguments().get("text");
        List<String> textList;
        if (existing instanceof List) {
          @SuppressWarnings("unchecked")
          List<String> existingList = (List<String>) existing;
          textList = existingList;
        } else {
          textList = new ArrayList<>();
          textList.add(existing.toString());
        }
        textList.add(text);
        currentDirective.addArgument("text", textList);
      } else {
        currentDirective.addArgument("text", text);
      }
    }
    
    return text;
  }
  
  /**
   * Called when a number is visited.
   */
  @Override
  public String visitNumber(DirectivesParser.NumberContext ctx) {
    String number = ctx.Number().getText();
    
    if (currentDirective != null) {
      try {
        if (number.contains(".")) {
          currentDirective.addArgument("number", Double.parseDouble(number));
        } else {
          currentDirective.addArgument("number", Integer.parseInt(number));
        }
      } catch (NumberFormatException e) {
        currentDirective.addArgument("number", number);
      }
    }
    
    return number;
  }
  
  /**
   * Called when a boolean is visited.
   */
  @Override
  public String visitBool(DirectivesParser.BoolContext ctx) {
    String bool = ctx.Bool().getText();
    
    if (currentDirective != null) {
      currentDirective.addArgument("bool", Boolean.parseBoolean(bool));
    }
    
    return bool;
  }
  
  /**
   * Called when an identifier is visited.
   */
  @Override
  public String visitIdentifier(DirectivesParser.IdentifierContext ctx) {
    String identifier = ctx.Identifier().getText();
    
    if (currentDirective != null) {
      currentDirective.addArgument("identifier", identifier);
    }
    
    return identifier;
  }
  
  /**
   * Called when a property list is visited.
   */
  @Override
  public String visitPropertyList(DirectivesParser.PropertyListContext ctx) {
    Map<String, Object> properties = new HashMap<>();
    
    for (DirectivesParser.PropertyContext propertyCtx : ctx.property()) {
      String key = propertyCtx.Identifier().getText();
      Object value = null;
      
      if (propertyCtx.number() != null) {
        String numStr = propertyCtx.number().Number().getText();
        try {
          if (numStr.contains(".")) {
            value = Double.parseDouble(numStr);
          } else {
            value = Integer.parseInt(numStr);
          }
        } catch (NumberFormatException e) {
          value = numStr;
        }
      } else if (propertyCtx.bool() != null) {
        value = Boolean.parseBoolean(propertyCtx.bool().getText());
      } else if (propertyCtx.text() != null) {
        String text = propertyCtx.text().String().getText();
        value = text.substring(1, text.length() - 1); // Remove quotes
      }
      
      properties.put(key, value);
    }
    
    if (currentDirective != null) {
      currentDirective.addArgument("properties", properties);
    }
    
    return properties.toString();
  }
  
  /**
   * Extract the original source text for a rule context.
   */
  private String getOriginalSource(ParserRuleContext ctx) {
    if (ctx == null || ctx.start == null || ctx.stop == null) {
      return "";
    }
    
    int startIndex = ctx.start.getStartIndex();
    int stopIndex = ctx.stop.getStopIndex();
    
    if (stopIndex < startIndex) {
      return "";
    }
    
    Interval interval = new Interval(startIndex, stopIndex);
    if (ctx.start.getInputStream() != null) {
      return ctx.start.getInputStream().getText(interval);
    }
    
    return "";
  }
}
