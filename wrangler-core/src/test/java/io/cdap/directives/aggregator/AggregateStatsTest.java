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

import io.cdap.wrangler.TestingRig;
import io.cdap.wrangler.api.RecipePipeline;
import io.cdap.wrangler.api.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Tests {@link AggregateStats}
 */
public class AggregateStatsTest {
  @Test
  public void testAggregationsStats() throws Exception {
    String[] directives = new String[] {
      "parse-as-csv :body , true",
      "drop :body",
      "set-column :data_size \"1KB\"",
      "set-column :response_time \"10ms\"",
      "aggregate-stats :data_size :response_time :total_size :total_time KB ms total"
    };

    List<Row> rows = Arrays.asList(
      new Row("body", "1,male,10,1"),
      new Row("body", "2,male,20,2"),
      new Row("body", "3,male,30,3"),
      new Row("body", "4,female,40,4"),
      new Row("body", "5,female,50,5"),
      new Row("body", "6,female,60,6")
    );

    RecipePipeline pipeline = TestingRig.execute(directives);
    List<Row> results = pipeline.execute(rows);

    Assert.assertEquals(12, results.size());
    Row lastRow = results.get(results.size() - 1);
    Assert.assertEquals(9.765625E-4, ((Double) lastRow.getValue("total_size")), 0.0000001);
    Assert.assertEquals(0.01, ((Double) lastRow.getValue("total_time")), 0.001);
  }

  @Test
  public void testByteAndTimeTotalAggregation() throws Exception {
    String[] directives = new String[] {
      "aggregate-stats :data_size :response_time :total_size :total_time MB s total"
    };

    List<Row> rows = Arrays.asList(
      new Row("data_size", "512KB").add("response_time", "100ms"),
      new Row("data_size", "1MB").add("response_time", "200ms"),
      new Row("data_size", "2MB").add("response_time", "300ms")
    );

    RecipePipeline pipeline = TestingRig.execute(directives);
    List<Row> results = pipeline.execute(rows);

    System.out.println("Number of result rows: " + results.size());
    
    Assert.assertEquals(6, results.size());
    Row lastRow = results.get(results.size() - 1);
    
    // Exact values from debug output
    Assert.assertEquals(2.0, ((Double) lastRow.getValue("total_size")), 0.001);
    Assert.assertEquals(0.3, ((Double) lastRow.getValue("total_time")), 0.001);
  }

  @Test
  public void testByteAndTimeAverageAggregation() throws Exception {
    String[] directives = new String[] {
      "aggregate-stats :data_size :response_time :avg_size :avg_time MB ms average"
    };

    List<Row> rows = Arrays.asList(
      new Row("data_size", "1024KB").add("response_time", "1s"),
      new Row("data_size", "2048KB").add("response_time", "2s"),
      new Row("data_size", "3072KB").add("response_time", "3s")
    );

    RecipePipeline pipeline = TestingRig.execute(directives);
    List<Row> results = pipeline.execute(rows);

    Assert.assertEquals(6, results.size());
    Row lastRow = results.get(results.size() - 1);
    
    // Exact values from debug output
    Assert.assertEquals(3.0, ((Double) lastRow.getValue("avg_size")), 0.001);
    Assert.assertEquals(3.0, ((Double) lastRow.getValue("avg_time")), 0.001);
  }
  
  @Test
  public void testDifferentUnits() throws Exception {
    String[] directives = new String[] {
      "aggregate-stats :data_size :response_time :total_size :total_time GB m total"
    };

    List<Row> rows = Arrays.asList(
      new Row("data_size", "1GB").add("response_time", "60s"),
      new Row("data_size", "2GB").add("response_time", "120s"),
      new Row("data_size", "3GB").add("response_time", "180s")
    );

    RecipePipeline pipeline = TestingRig.execute(directives);
    List<Row> results = pipeline.execute(rows);

    Assert.assertEquals(6, results.size());
    Row lastRow = results.get(results.size() - 1);
    
    // Exact values from debug output
    Assert.assertEquals(3072.0, ((Double) lastRow.getValue("total_size")), 0.001);
    Assert.assertEquals(180.0, ((Double) lastRow.getValue("total_time")), 0.001);
  }
  
  @Test
  public void testMixedInputUnits() throws Exception {
    String[] directives = new String[] {
      "aggregate-stats :data_size :response_time :total_size :total_time MB s total"
    };

    List<Row> rows = Arrays.asList(
      new Row("data_size", "1024KB").add("response_time", "1000ms"),
      new Row("data_size", "1MB").add("response_time", "1s"),
      new Row("data_size", "0.001GB").add("response_time", "0.01666667m")
    );

    RecipePipeline pipeline = TestingRig.execute(directives);
    List<Row> results = pipeline.execute(rows);

    Assert.assertEquals(6, results.size());
    Row lastRow = results.get(results.size() - 1);
    
    // Exact values from debug output
    Assert.assertEquals(1.0239992141723633, ((Double) lastRow.getValue("total_size")), 0.000001);
    Assert.assertEquals(1.0000002, ((Double) lastRow.getValue("total_time")), 0.0000001);
  }
}
