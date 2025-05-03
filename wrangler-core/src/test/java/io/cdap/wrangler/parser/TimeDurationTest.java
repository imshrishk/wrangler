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

import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link TimeDuration}
 */
public class TimeDurationTest {

  @Test
  public void testParsing() {
    TimeDuration t1 = new TimeDuration("1ns");
    Assert.assertEquals(1, t1.toNanos());
    Assert.assertEquals("1ns", t1.value());
    Assert.assertEquals("TIME_DURATION", t1.type().name());

    TimeDuration t2 = new TimeDuration("1us");
    Assert.assertEquals(1_000, t2.toNanos());

    TimeDuration t3 = new TimeDuration("1ms");
    Assert.assertEquals(1_000_000, t3.toNanos());

    TimeDuration t4 = new TimeDuration("1s");
    Assert.assertEquals(1_000_000_000, t4.toNanos());

    TimeDuration t5 = new TimeDuration("1m");
    Assert.assertEquals(60 * 1_000_000_000L, t5.toNanos());

    TimeDuration t6 = new TimeDuration("1h");
    Assert.assertEquals(60 * 60 * 1_000_000_000L, t6.toNanos());

    TimeDuration t7 = new TimeDuration("1d");
    Assert.assertEquals(24 * 60 * 60 * 1_000_000_000L, t7.toNanos());

    TimeDuration t8 = new TimeDuration("1D");
    Assert.assertEquals(24 * 60 * 60 * 1_000_000_000L, t8.toNanos());
  }

  @Test
  public void testToUnitConversions() {
    TimeDuration duration = new TimeDuration("1s");
    
    Assert.assertEquals(1_000_000_000, duration.to("ns"), 0.001);
    Assert.assertEquals(1_000_000, duration.to("us"), 0.001);
    Assert.assertEquals(1_000, duration.to("ms"), 0.001);
    Assert.assertEquals(1, duration.to("s"), 0.001);
    Assert.assertEquals(1.0 / 60, duration.to("m"), 0.001);
    Assert.assertEquals(1.0 / (60 * 60), duration.to("h"), 0.001);
    Assert.assertEquals(1.0 / (24 * 60 * 60), duration.to("d"), 0.001);
  }
  
  @Test
  public void testToConvenienceMethods() {
    TimeDuration duration = new TimeDuration("1s");
    
    Assert.assertEquals(1000, duration.toMillis(), 0.001);
    Assert.assertEquals(1, duration.toSeconds(), 0.001);
  }
}
