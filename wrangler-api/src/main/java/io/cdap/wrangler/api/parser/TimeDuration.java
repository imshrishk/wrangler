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
 * A {@link Token} implementation for time duration values (e.g. "100ms", "5s", "2m", "1h").
 */
public class TimeDuration implements Token {
  // Enhanced pattern to support more time unit formats and full unit names
  private static final Pattern TIME_PATTERN = Pattern.compile(
      "^(\\d+(\\.\\d+)?)\\s*(ns|nanoseconds?|us|μs|microseconds?|ms|" +
      "milliseconds?|s|sec|seconds?|m|min|minutes?|h|hr|hours?|d|days?)$", 
      Pattern.CASE_INSENSITIVE);
      
  private final String originalValue;
  private final double value;
  private final String unit;
  
  /**
   * Constructs a TimeDuration token from a string representation.
   *
   * @param value string representation of a time duration (e.g. "100ms", "5s", "2min", "1hour")
   * @throws IllegalArgumentException if the value doesn't match the expected pattern
   */
  public TimeDuration(String value) {
    this.originalValue = value;
    Matcher matcher = TIME_PATTERN.matcher(value.trim());
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Invalid time duration format: " + value + 
          ". Expected format examples: 10ns, 100ms, 1.5s, 2m, 3hr, 1day");
    }
    
    this.value = Double.parseDouble(matcher.group(1));
    if (this.value < 0) {
      throw new IllegalArgumentException("Time duration cannot be negative: " + value);
    }
    
    String unitPart = matcher.group(3).toLowerCase();
    // Normalize the unit
    if (unitPart.startsWith("nanosecond")) {
      this.unit = "ns";
    } else if (unitPart.startsWith("microsecond") || unitPart.equals("us") || unitPart.equals("μs")) {
      this.unit = "us";
    } else if (unitPart.startsWith("millisecond")) {
      this.unit = "ms";
    } else if (unitPart.startsWith("second") || unitPart.equals("sec")) {
      this.unit = "s";
    } else if (unitPart.startsWith("minute") || unitPart.equals("min")) {
      this.unit = "m";
    } else if (unitPart.startsWith("hour") || unitPart.equals("hr")) {
      this.unit = "h";
    } else if (unitPart.startsWith("day")) {
      this.unit = "d";
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
    return TokenType.TIME_DURATION;
  }
  
  @Override
  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.add("type", new JsonPrimitive(type().name()));
    object.add("value", new JsonPrimitive(originalValue));
    return object;
  }
  
  /**
   * Returns the time duration in nanoseconds.
   *
   * @return time in nanoseconds
   */
  public long toNanos() {
    switch (unit) {
      case "ns":
        return (long) value;
      case "us":
        return (long) (value * 1_000);
      case "ms":
        return (long) (value * 1_000_000);
      case "s":
        return (long) (value * 1_000_000_000);
      case "m":
        return (long) (value * 60 * 1_000_000_000L);
      case "h":
        return (long) (value * 60 * 60 * 1_000_000_000L);
      case "d":
        return (long) (value * 24 * 60 * 60 * 1_000_000_000L);
      default:
        throw new IllegalStateException("Unknown time unit: " + unit);
    }
  }
  
  /**
   * Returns the time duration in microseconds.
   *
   * @return time in microseconds
   */
  public double toMicros() {
    return toNanos() / 1_000.0;
  }
  
  /**
   * Returns the time duration in milliseconds.
   *
   * @return time in milliseconds
   */
  public double toMillis() {
    return toNanos() / 1_000_000.0;
  }
  
  /**
   * Returns the time duration in seconds.
   *
   * @return time in seconds
   */
  public double toSeconds() {
    return toNanos() / 1_000_000_000.0;
  }
  
  /**
   * Returns the time duration in minutes.
   *
   * @return time in minutes
   */
  public double toMinutes() {
    return toSeconds() / 60.0;
  }
  
  /**
   * Returns the time duration in hours.
   *
   * @return time in hours
   */
  public double toHours() {
    return toMinutes() / 60.0;
  }
  
  /**
   * Returns the time duration in days.
   *
   * @return time in days
   */
  public double toDays() {
    return toHours() / 24.0;
  }
  
  /**
   * Returns the time duration in the specified unit.
   *
   * @param unit the unit to convert to (ns, us, ms, s, m, h, d)
   * @return time in the specified unit
   */
  public double to(String unit) {
    switch (unit.toLowerCase()) {
      case "ns":
      case "nanosecond":
      case "nanoseconds":
        return toNanos();
      case "us":
      case "μs":
      case "microsecond":
      case "microseconds":
        return toMicros();
      case "ms":
      case "millisecond":
      case "milliseconds":
        return toMillis();
      case "s":
      case "sec":
      case "second":
      case "seconds":
        return toSeconds();
      case "m":
      case "min":
      case "minute":
      case "minutes":
        return toMinutes();
      case "h":
      case "hr":
      case "hour":
      case "hours":
        return toHours();
      case "d":
      case "day":
      case "days":
        return toDays();
      default:
        throw new IllegalArgumentException("Unknown time unit: " + unit);
    }
  }
  
  /**
   * Provides a human-readable representation of the time duration.
   * 
   * @return String representation of the time duration
   */
  @Override
  public String toString() {
    return originalValue;
  }
}
