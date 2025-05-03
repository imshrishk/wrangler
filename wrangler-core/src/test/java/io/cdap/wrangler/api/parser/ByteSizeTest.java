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

import org.junit.Assert;
import org.junit.Test;

public class ByteSizeTest {
  
  @Test
  public void testByteSizeParsing() {
    ByteSize b1 = new ByteSize("1B");
    Assert.assertEquals(1, b1.toBytes());
    
    ByteSize b2 = new ByteSize("1KB");
    Assert.assertEquals(1024, b2.toBytes());
    Assert.assertEquals(1.0, b2.toKB(), 0.00001);
    
    ByteSize b3 = new ByteSize("1MB");
    Assert.assertEquals(1024 * 1024, b3.toBytes());
    Assert.assertEquals(1.0, b3.toMB(), 0.00001);
    
    ByteSize b4 = new ByteSize("1GB");
    Assert.assertEquals(1024 * 1024 * 1024, b4.toBytes());
    Assert.assertEquals(1.0, b4.toGB(), 0.00001);
    
    ByteSize b5 = new ByteSize("1TB");
    Assert.assertEquals(1024L * 1024L * 1024L * 1024L, b5.toBytes());
    Assert.assertEquals(1.0, b5.toTB(), 0.00001);
  }
  
  @Test
  public void testFractionalValues() {
    ByteSize b1 = new ByteSize("1.5KB");
    Assert.assertEquals(1536, b1.toBytes());
    Assert.assertEquals(1.5, b1.toKB(), 0.00001);
    
    ByteSize b2 = new ByteSize("2.5MB");
    Assert.assertEquals(2.5, b2.toMB(), 0.00001);
  }
  
  @Test
  public void testConversion() {
    ByteSize b = new ByteSize("1024KB");
    Assert.assertEquals(1, b.toMB(), 0.00001);
    Assert.assertEquals(1024, b.toKB(), 0.00001);
    Assert.assertEquals(0.0009765625, b.toGB(), 0.00001);
    
    Assert.assertEquals(1024, b.to("KB"), 0.00001);
    Assert.assertEquals(1, b.to("MB"), 0.00001);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new ByteSize("notANumber");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    new ByteSize("10XB");
  }
  
  @Test
  public void testCaseInsensitivity() {
    ByteSize b1 = new ByteSize("1kb");
    ByteSize b2 = new ByteSize("1KB");
    
    Assert.assertEquals(b1.toBytes(), b2.toBytes());
    Assert.assertEquals(1.0, b1.toKB(), 0.00001);
  }
}
