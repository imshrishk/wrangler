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

package io.cdap.directives.aggregator;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Optional;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TransientVariableScope;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Directive to aggregate statistics from byte size and time duration columns.
 */
@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
@Description("Aggregates byte size and time duration statistics from specified columns")
public class AggregateStats implements Directive {
  
  public static final String BYTE_SIZE_COL = "bytesize_column";
  public static final String TIME_DURATION_COL = "timeduration_column";
  public static final String SIZE_TARGET_COL = "size_target_column";
  public static final String TIME_TARGET_COL = "time_target_column";
  public static final String SIZE_UNIT = "size_unit";
  public static final String TIME_UNIT = "time_unit";
  public static final String AGGREGATION_TYPE = "aggregation_type";
  
  // Class variables to store aggregation state
  private String byteSizeColumn;
  private String timeDurationColumn;
  private String sizeTargetColumn;
  private String timeTargetColumn;
  private String sizeUnit;
  private String timeUnit;
  private String aggregationType;
  
  // Aggregation counters
  private long totalBytes;
  private long totalNanoseconds;
  private int rowCount;
  
  /**
   * Defines the usage of the directive with example and description.
   *
   * @return A {@link UsageDefinition} object.
   */
  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
    
    builder.define(BYTE_SIZE_COL, TokenType.COLUMN_NAME, 
                  "Column containing byte size values (e.g., 10KB, 1.5MB)");
    builder.define(TIME_DURATION_COL, TokenType.COLUMN_NAME, 
                  "Column containing time duration values (e.g., 100ms, 1.5s)");
    builder.define(SIZE_TARGET_COL, TokenType.COLUMN_NAME, "Target column name for aggregated size");
    builder.define(TIME_TARGET_COL, TokenType.COLUMN_NAME, "Target column name for aggregated time");
    
    // Optional arguments
    builder.define(SIZE_UNIT, TokenType.TEXT, "Optional unit for size output (B, KB, MB, GB, TB)", 
                  Optional.TRUE);
    builder.define(TIME_UNIT, TokenType.TEXT, "Optional unit for time output (ns, ms, s, m, h)", 
                  Optional.TRUE);
    builder.define(AGGREGATION_TYPE, TokenType.TEXT, "Optional aggregation type (total, average)", 
                  Optional.TRUE);
    
    return builder.build();
  }
  
  /**
   * Initializes the directive with arguments provided.
   *
   * @param arguments Arguments for the directive.
   */
  @Override
  public void initialize(Arguments arguments) throws DirectiveParseException {
    this.byteSizeColumn = ((ColumnName) arguments.value(BYTE_SIZE_COL)).value();
    this.timeDurationColumn = ((ColumnName) arguments.value(TIME_DURATION_COL)).value();
    this.sizeTargetColumn = ((ColumnName) arguments.value(SIZE_TARGET_COL)).value();
    this.timeTargetColumn = ((ColumnName) arguments.value(TIME_TARGET_COL)).value();
    
    if (arguments.contains(SIZE_UNIT)) {
      this.sizeUnit = ((Text) arguments.value(SIZE_UNIT)).value();
    } else {
      this.sizeUnit = "MB";
    }
    
    if (arguments.contains(TIME_UNIT)) {
      this.timeUnit = ((Text) arguments.value(TIME_UNIT)).value();
    } else {
      this.timeUnit = "s";
    }
    
    if (arguments.contains(AGGREGATION_TYPE)) {
      this.aggregationType = ((Text) arguments.value(AGGREGATION_TYPE)).value();
    } else {
      this.aggregationType = "total";
    }
    
    // Reset the aggregation counters
    resetCounters();
  }
  
  // Helper method to reset counters for a clean state
  private void resetCounters() {
    this.totalBytes = 0;
    this.totalNanoseconds = 0;
    this.rowCount = 0;
  }
  
  /**
   * Executes the directive with the given rows.
   *
   * @param rows List of rows to process
   * @param context The execution context
   * @return List of rows - unchanged for normal rows, or a single row with aggregation results at the end
   */
  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    // Reset counters to ensure fresh aggregation
    resetCounters();
    
    // Process each row
    for (Row row : rows) {
      // Skip rows with missing columns
      if (row.find(byteSizeColumn) == -1 || row.find(timeDurationColumn) == -1) {
        continue;
      }
      
      try {
        // Extract byte size and add to total
        Object byteSizeObj = row.getValue(byteSizeColumn);
        String byteSizeStr = byteSizeObj.toString();
        ByteSize byteSize = new ByteSize(byteSizeStr);
        totalBytes += byteSize.toBytes();
        
        // Extract time duration and add to total
        Object timeDurationObj = row.getValue(timeDurationColumn);
        String timeDurationStr = timeDurationObj.toString();
        TimeDuration timeDuration = new TimeDuration(timeDurationStr);
        totalNanoseconds += timeDuration.toNanos();
        
        rowCount++;
      } catch (Exception e) {
        throw new DirectiveExecutionException(
          String.format("Error processing values for aggregation: %s", e.getMessage()), e);
      }
    }
    
    // Store aggregate information in the context
    @SuppressWarnings("unchecked")
    Map<String, Object> aggregateMap = (Map<String, Object>) context.getTransientStore().get("aggregate-stats");
    if (aggregateMap == null) {
      aggregateMap = new HashMap<>();
      context.getTransientStore().set(TransientVariableScope.GLOBAL, "aggregate-stats", aggregateMap);
    }
    
    aggregateMap.put("totalBytes", totalBytes);
    aggregateMap.put("totalNanoseconds", totalNanoseconds);
    aggregateMap.put("rowCount", rowCount);
    aggregateMap.put("sizeUnit", sizeUnit);
    aggregateMap.put("timeUnit", timeUnit);
    aggregateMap.put("aggregationType", aggregationType);
    aggregateMap.put("sizeTargetColumn", sizeTargetColumn);
    aggregateMap.put("timeTargetColumn", timeTargetColumn);
    
    // Create a new list for output rows instead of modifying the input
    List<Row> results = new ArrayList<>();
    
    // First, add all the original rows
    results.addAll(rows);
    
    // Create and add the aggregate row to the results
    Row resultRow = new Row();
    
    if (aggregationType.equalsIgnoreCase("average") && rowCount > 0) {
      // Calculate averages
      double averageBytes = (double) totalBytes / rowCount;
      double averageNanoseconds = (double) totalNanoseconds / rowCount;
      
      // Convert to specified units
      double sizeInTargetUnit = convertBytesToUnit(averageBytes, sizeUnit);
      double timeInTargetUnit = convertNanosecondsToUnit(averageNanoseconds, timeUnit);
      
      resultRow.add(sizeTargetColumn, sizeInTargetUnit);
      resultRow.add(timeTargetColumn, timeInTargetUnit);
      resultRow.add("count", rowCount);
    } else {
      // Calculate totals (default)
      double sizeInTargetUnit = convertBytesToUnit(totalBytes, sizeUnit);
      double timeInTargetUnit = convertNanosecondsToUnit(totalNanoseconds, timeUnit);
      
      resultRow.add(sizeTargetColumn, sizeInTargetUnit);
      resultRow.add(timeTargetColumn, timeInTargetUnit);
      resultRow.add("count", rowCount);
    }
    
    results.add(resultRow);
    return results;
  }
  
  /**
   * Finalizes the directive and releases resources.
   */
  @Override
  public void destroy() {
    // Reset the aggregation values
    this.totalBytes = 0L;
    this.totalNanoseconds = 0L;
    this.rowCount = 0;
  }
  
  /**
   * Convert bytes to the specified unit.
   *
   * @param bytes Number of bytes
   * @param unit Target unit (B, KB, MB, GB, TB)
   * @return Value in the target unit
   */
  private double convertBytesToUnit(double bytes, String unit) {
    System.out.println("Converting " + bytes + " bytes to " + unit);
    double result = 0.0;
    
    // Handle conversion based on target unit
    switch (unit.toUpperCase()) {
      case "B":
        result = bytes;
        break;
      case "KB":
        result = bytes / 1024.0;
        break;
      case "MB":
        result = bytes / (1024.0 * 1024.0);
        break;
      case "GB":
        result = bytes / (1024.0 * 1024.0 * 1024.0);
        break;
      case "TB":
        result = bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
        break;
      default:
        throw new IllegalArgumentException("Unsupported byte unit: " + unit);
    }
    
    System.out.println("Conversion result: " + result);
    return result;
  }
  
  /**
   * Convert nanoseconds to the specified unit.
   *
   * @param nanos Nanoseconds to convert
   * @param unit Target unit
   * @return Value in the target unit
   */
  private double convertNanosecondsToUnit(double nanos, String unit) {
    switch (unit.toLowerCase()) {
      case "ns":
        return nanos;
      case "ms":
        return nanos / 1_000_000.0;
      case "s":
        return nanos / 1_000_000_000.0;
      case "m":
        return nanos / (60.0 * 1_000_000_000.0);
      case "h":
        return nanos / (60.0 * 60.0 * 1_000_000_000.0);
      default:
        return nanos;
    }
  }
}
