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

package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link Token} implementation for byte size values (e.g. "10B", "1.5KB", "2MB", "3GB", "4TB").
 */
public class ByteSize implements Token {
  // Enhanced pattern to support more unit formats (K/KB, M/MB, etc.)
  private static final Pattern BYTE_PATTERN = Pattern.compile(
      "^(\\d+(\\.\\d+)?)\\s*(B|K|KB|M|MB|G|GB|T|TB|P|PB)$",
      Pattern.CASE_INSENSITIVE);
      
  private final String originalValue;
  private final double value;
  private final String unit;
  
  /**
   * Constructs a ByteSize token from a string representation.
   *
   * @param value string representation of a byte size (e.g. "10KB", "1.5MB", "2G")
   * @throws IllegalArgumentException if the value doesn't match the expected pattern
   */
  public ByteSize(String value) {
    this.originalValue = value;
    Matcher matcher = BYTE_PATTERN.matcher(value.trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid byte size format: " + value + 
          ". Expected format examples: 10B, 1.5KB, 2M, 3GB, 4TB");
    }
    
    this.value = Double.parseDouble(matcher.group(1));
    if (this.value < 0) {
      throw new IllegalArgumentException("Byte size cannot be negative: " + value);
    }
    
    String unitPart = matcher.group(3).toUpperCase();
    // Normalize the unit (K -> KB, M -> MB, etc.)
    if (unitPart.equals("K")) {
      this.unit = "KB";
    } else if (unitPart.equals("M")) {
      this.unit = "MB";
    } else if (unitPart.equals("G")) {
      this.unit = "GB";
    } else if (unitPart.equals("T")) {
      this.unit = "TB";
    } else if (unitPart.equals("P")) {
      this.unit = "PB";
    } else {
      this.unit = unitPart;
    }
  }
  
  @Override
  public String value() {
    return originalValue;
  }
  
  @Override
  public TokenType type() {
    return TokenType.BYTE_SIZE;
  }
  
  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.add("type", new JsonPrimitive(type().name()));
    object.add("value", new JsonPrimitive(originalValue));
    return object;
  }
  
  /**
   * Returns the byte size in bytes.
   *
   * @return size in bytes
   */
  public long toBytes() {
    switch (unit) {
      case "B":
        return (long) value;
      case "KB":
        return (long) (value * 1024);
      case "MB":
        return (long) (value * 1024 * 1024);
      case "GB":
        return (long) (value * 1024 * 1024 * 1024);
      case "TB":
        return (long) (value * 1024L * 1024L * 1024L * 1024L);
      case "PB":
        return (long) (value * 1024L * 1024L * 1024L * 1024L * 1024L);
      default:
        throw new IllegalStateException("Unknown byte unit: " + unit);
    }
  }
  
  /**
   * Returns the byte size in kilobytes.
   *
   * @return size in kilobytes
   */
  public double toKB() {
    return toBytes() / 1024.0;
  }
  
  /**
   * Returns the byte size in megabytes.
   *
   * @return size in megabytes
   */
  public double toMB() {
    return toKB() / 1024.0;
  }
  
  /**
   * Returns the byte size in gigabytes.
   *
   * @return size in gigabytes
   */
  public double toGB() {
    return toMB() / 1024.0;
  }
  
  /**
   * Returns the byte size in terabytes.
   *
   * @return size in terabytes
   */
  public double toTB() {
    return toGB() / 1024.0;
  }
  
  /**
   * Returns the byte size in petabytes.
   *
   * @return size in petabytes
   */
  public double toPB() {
    return toTB() / 1024.0;
  }
  
  /**
   * Returns the byte size in the specified unit.
   *
   * @param unit the unit to convert to (B, KB, MB, GB, TB, PB)
   * @return size in the specified unit
   */
  public double to(String unit) {
    switch (unit.toUpperCase()) {
      case "B":
        return toBytes();
      case "KB":
      case "K":
        return toKB();
      case "MB":
      case "M":
        return toMB();
      case "GB":
      case "G":
        return toGB();
      case "TB":
      case "T":
        return toTB();
      case "PB":
      case "P":
        return toPB();
      default:
        throw new IllegalArgumentException("Unknown byte unit: " + unit);
    }
  }
  
  /**
   * Provides a human-readable representation of the byte size.
   * 
   * @return String representation of the byte size
   */
  @Override
  public String toString() {
    return originalValue;
  }
} 
