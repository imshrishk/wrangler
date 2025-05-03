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

public class TimeDurationTest {
  
  @Test
  public void testTimeDurationParsing() {
    TimeDuration t1 = new TimeDuration("1ns");
    Assert.assertEquals(1, t1.toNanos());
    Assert.assertEquals(0.000001, t1.toMillis(), 0.00001);
    
    TimeDuration t2 = new TimeDuration("1ms");
    Assert.assertEquals(1_000_000, t2.toNanos());
    Assert.assertEquals(1.0, t2.toMillis(), 0.00001);
    
    TimeDuration t3 = new TimeDuration("1s");
    Assert.assertEquals(1_000_000_000, t3.toNanos());
    Assert.assertEquals(1.0, t3.toSeconds(), 0.00001);
    
    TimeDuration t4 = new TimeDuration("1m");
    Assert.assertEquals(60_000_000_000L, t4.toNanos());
    Assert.assertEquals(1.0, t4.toMinutes(), 0.00001);
    
    TimeDuration t5 = new TimeDuration("1h");
    Assert.assertEquals(3600_000_000_000L, t5.toNanos());
    Assert.assertEquals(1.0, t5.toHours(), 0.00001);
  }
  
  @Test
  public void testFractionalValues() {
    TimeDuration t1 = new TimeDuration("1.5ms");
    Assert.assertEquals(1_500_000, t1.toNanos());
    Assert.assertEquals(1.5, t1.toMillis(), 0.00001);
    
    TimeDuration t2 = new TimeDuration("2.5s");
    Assert.assertEquals(2.5, t2.toSeconds(), 0.00001);
  }
  
  @Test
  public void testConversion() {
    TimeDuration t = new TimeDuration("1000ms");
    Assert.assertEquals(1, t.toSeconds(), 0.00001);
    Assert.assertEquals(1000, t.toMillis(), 0.00001);
    Assert.assertEquals(0.016666, t.toMinutes(), 0.00001);
    
    Assert.assertEquals(1000, t.to("ms"), 0.00001);
    Assert.assertEquals(1, t.to("s"), 0.00001);
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidFormat() {
    new TimeDuration("notANumber");
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    new TimeDuration("10xy");
  }
}
