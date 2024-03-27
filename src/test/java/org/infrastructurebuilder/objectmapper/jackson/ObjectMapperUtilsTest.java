/*
 * @formatter:off
 * Copyright © 2019 admin (admin@infrastructurebuilder.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * @formatter:on
 */
package org.infrastructurebuilder.objectmapper.jackson;

import static org.infrastructurebuilder.objectmapper.jackson.ObjectMapperUtils.MODEL_VERSION_KEY;
import static org.infrastructurebuilder.objectmapper.jackson.ObjectMapperUtils.TEMPKEY;
import static org.infrastructurebuilder.objectmapper.jackson.ObjectMapperUtils.TEMPVALUE;
import static org.infrastructurebuilder.objectmapper.jackson.ObjectMapperUtils.modelConvertIBResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ObjectMapperUtilsTest {

  private static final String LATEST = "1.0";

  @BeforeAll
  static void setUpBeforeClass() throws Exception {
  }

  @AfterAll
  static void tearDownAfterClass() throws Exception {
  }

  private JSONObject in;

  @BeforeEach
  void setUp() throws Exception {
    in = new JSONObject();
    in.put(MODEL_VERSION_KEY, "0.9");
  }

  @AfterEach
  void tearDown() throws Exception {
  }

  @Test
  void testCons() {
    assertNotNull(new ObjectMapperUtils());
    assertNotNull(ObjectMapperUtils.mapper.get());
  }

  @Test
  void testModelConvertIBResource() {
    String outString = modelConvertIBResource(in.toString(), LATEST).orElse(null);
    JSONObject out = new JSONObject(outString);
    assertTrue(out.has(MODEL_VERSION_KEY));
    assertTrue(out.has(TEMPKEY));
    assertEquals(TEMPVALUE, out.getString(TEMPKEY));
    assertFalse(out.has("SOMEFAKEKEY"));
    var nn = modelConvertIBResource(out.toString(), LATEST);
    assertTrue(nn.isPresent());
  }

  @Test
  void testBad0_8() {
    in.put(MODEL_VERSION_KEY, "0.8");
    String outString = modelConvertIBResource(in.toString(), LATEST).orElse(null);
    assertNull(outString);
  }

  @Test
  void testUnknownVersion() {
    in.put(MODEL_VERSION_KEY, "ABC");
    var v = modelConvertIBResource(in.toString(), LATEST);
    assertTrue(v.isEmpty());
  }

  @Test
  void nullTest() {
    assertTrue(modelConvertIBResource(null, LATEST).isEmpty());
    assertTrue(ObjectMapperUtils.from0_9(null).isEmpty());
  }

}
