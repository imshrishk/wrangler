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

import io.cdap.wrangler.api.parser.ByteSize;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link ByteSize}
 */
public class ByteSizeTest {

  @Test
  public void testParsing() {
    ByteSize b1 = new ByteSize("1B");
    Assert.assertEquals(1, b1.toBytes());
    Assert.assertEquals("1B", b1.value());
    Assert.assertEquals("BYTE_SIZE", b1.type().name());

    ByteSize b2 = new ByteSize("1KB");
    Assert.assertEquals(1024, b2.toBytes());

    ByteSize b3 = new ByteSize("1MB");
    Assert.assertEquals(1024 * 1024, b3.toBytes());

    ByteSize b4 = new ByteSize("1GB");
    Assert.assertEquals(1024 * 1024 * 1024, b4.toBytes());

    ByteSize b5 = new ByteSize("1TB");
    Assert.assertEquals(1024L * 1024L * 1024L * 1024L, b5.toBytes());

    ByteSize b6 = new ByteSize("1PB");
    Assert.assertEquals(1024L * 1024L * 1024L * 1024L * 1024L, b6.toBytes());

    ByteSize b7 = new ByteSize("1pb");
    Assert.assertEquals(1024L * 1024L * 1024L * 1024L * 1024L, b7.toBytes());
  }

  @Test
  public void testToByteConversions() {
    ByteSize byteSize = new ByteSize("1024KB");
    
    Assert.assertEquals(1024, byteSize.to("KB"), 0.001);
    Assert.assertEquals(1, byteSize.to("MB"), 0.001);
    Assert.assertEquals(0.001, byteSize.to("GB"), 0.001);
    Assert.assertEquals(0.000001, byteSize.to("TB"), 0.000001);
    Assert.assertEquals(0.000000001, byteSize.to("PB"), 0.000000001);
    Assert.assertEquals(1048576, byteSize.to("B"), 0.001);
  }
  
  @Test
  public void testAbbreviatedUnits() {
    // Test the abbreviated unit format support (K, M, G, T, P without the B)
    ByteSize b1 = new ByteSize("1K");
    Assert.assertEquals(1024, b1.toBytes());
    
    ByteSize b2 = new ByteSize("1M");
    Assert.assertEquals(1024 * 1024, b2.toBytes());
    
    ByteSize b3 = new ByteSize("1G");
    Assert.assertEquals(1024 * 1024 * 1024, b3.toBytes());
    
    ByteSize b4 = new ByteSize("1T");
    Assert.assertEquals(1024L * 1024L * 1024L * 1024L, b4.toBytes());
    
    ByteSize b5 = new ByteSize("1P");
    Assert.assertEquals(1024L * 1024L * 1024L * 1024L * 1024L, b5.toBytes());
  }
  
  @Test
  public void testSpacedUnits() {
    // Test with spaces between the value and unit
    ByteSize b1 = new ByteSize("1 KB");
    Assert.assertEquals(1024, b1.toBytes());
    
    ByteSize b2 = new ByteSize("1 MB");
    Assert.assertEquals(1024 * 1024, b2.toBytes());
    
    ByteSize b3 = new ByteSize("1.5 GB");
    Assert.assertEquals(1.5 * 1024 * 1024 * 1024, b3.toBytes(), 1);
  }
  
  @Test
  public void testEdgeCases() {
    // Test zero values
    ByteSize zero1 = new ByteSize("0B");
    Assert.assertEquals(0, zero1.toBytes());
    
    ByteSize zero2 = new ByteSize("0KB");
    Assert.assertEquals(0, zero2.toBytes());
    
    // Test very small decimal values
    ByteSize small = new ByteSize("0.001MB");
    Assert.assertEquals(1048.576, small.toBytes(), 1.0);
    
    // Test large decimal values
    ByteSize large = new ByteSize("123456.789MB");
    Assert.assertEquals(129441505173.504, large.toBytes(), 100000000.0);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new ByteSize("not a byte size");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testNegativeValue() {
    new ByteSize("-10MB");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    new ByteSize("10ZB"); // ZB is not a supported unit
  }
  
  @Test
  public void testToStringAndValueConsistency() {
    String original = "1.5MB";
    ByteSize byteSize = new ByteSize(original);
    
    Assert.assertEquals(original, byteSize.value());
    Assert.assertEquals(original, byteSize.toString());
  }
}
