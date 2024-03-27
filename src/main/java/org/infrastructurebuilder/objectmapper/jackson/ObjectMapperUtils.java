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

import java.util.Optional;
import java.util.function.Supplier;

import org.json.JSONException;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ObjectMapperUtils {
  static final String TEMPVALUE = "TEMPVALUE";
  static final String TEMPKEY = "TEMPKEY";
  public static final String MODEL_VERSION_KEY = "modelVersion";
  public final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ObjectMapperUtils.class);
  public final static Supplier<ObjectMapper> mapper = () -> new ObjectMapper() //
      .registerModule(//
          new JavaTimeModule() //
              .enable(JavaTimeFeature.ALWAYS_ALLOW_STRINGIFIED_DATE_TIMESTAMPS))
      .registerModule(//
          new Jdk8Module() //
      );

  /**
   *
   * Function to convert a JSON (as a string) representation of an IBResource from one version to another
   *
   * This is inherently dangerous. It is performed as a straight manipulation of the existing data, and thus might be
   * impossible (leading to an "empty" result)
   *
   * The inbound JSON must have a field called "modelVersion" that is an API version of the desired model
   *
   * When a new model version is released, this must be updated to convert from the prior version to the latest version,
   * if necessary.
   *
   * @param inJSON        String of some version of the IBResourceModel
   * @param targetVersion Target API version of IBResourceModel (expected to be the "latest" version included as a
   *                      dependency)
   * @return Optional JSON string if conversion was performed
   */
  public final static Optional<String> modelConvertIBResource(String inJSON, String targetVersion) {
    try {
      String result = inJSON;
      boolean done = false;
      JSONObject in = new JSONObject(result);
      String startVersion = in.getString(MODEL_VERSION_KEY);
      String modelVersion;
      while (in != null && !done) { // loop reworks the "in" variable
        switch (in.getString(MODEL_VERSION_KEY)) {
        case "1.0": // "latest" model here, so no changes
          done = true;
          break;
        case "0.9": // EXAMPLE
          in = from0_9(in).orElse(null);
          // Not done
          break;
        case "0.8": // BAD Example
          in = fake_from0_8(in).orElse(null);
          // Didn't reset done...
          break;
        default:
          in = null;
          done = true;
        }
        if (in != null && !done) {
          modelVersion = in.getString(MODEL_VERSION_KEY);
          if (modelVersion.equals(startVersion)) // No changed version
            in = null;
        }
        startVersion = in.getString(MODEL_VERSION_KEY);
      }
      return Optional.ofNullable(in).map(j -> j.toString(2));
    } catch (Throwable j) {
      log.error("Error in modelConvertIBResource", j);
      return Optional.empty();
    }
  }

  // We can write functions that transform old models into new ones, or that invalidate them
  // by returning empty(). The transformation must update the MODEL_VERSION_KEY value to some
  // new value (not necessarily the intended target value) or the processing will abort.

  /**
   * Example conversion function
   *
   * @param in Existing model JSONObject
   * @return updated JSONObject or empty()
   */
  final static Optional<JSONObject> from0_9(JSONObject in) {
    if (in != null) {
      // Just an example
      JSONObject j = new JSONObject(in.toString());
      j.put(TEMPKEY, TEMPVALUE);
      j.put(MODEL_VERSION_KEY, "1.0"); // Update the model version
      in = j;
    }
    return Optional.ofNullable(in);
  }

  final static Optional<JSONObject> fake_from0_8(JSONObject in) { // JUST A BAD EXAMPLE
    // Just an example
    JSONObject j = new JSONObject(in.toString());
    j.put(TEMPKEY, TEMPVALUE);
//    j.put(MODEL_VERSION_KEY, "1.0"); // FAIL TO UPDATE! THIS WON'T WORK
    in = j;
    return Optional.ofNullable(in);
  }
}
