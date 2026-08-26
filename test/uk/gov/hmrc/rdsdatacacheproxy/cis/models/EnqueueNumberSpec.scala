/*
 * Copyright 2025 HM Revenue & Customs
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
 */

package uk.gov.hmrc.rdsdatacacheproxy.cis.models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, Json}

class EnqueueNumberSpec extends AnyFreeSpec with Matchers {

  "EnqueueNumber JSON format" - {

    "read from JSON correctly" in {
      val json = Json.obj(
        "dataType" -> 1,
        "payload" -> Json.obj(
          "EVENT_TYPE" -> 1010L
        )
      )

      val result = json.as[EnqueueNumber]

      result mustBe EnqueueNumber(
        dataType = 1,
        payload = Map(
          "EVENT_TYPE" -> 1010L
        )
      )
    }

    "write to JSON correctly" in {
      val model = EnqueueNumber(
        dataType = 1,
        payload = Map(
          "EVENT_TYPE" -> 1010L
        )
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "dataType" -> 1,
        "payload" -> Json.obj(
          "EVENT_TYPE" -> 1010L
        )
      )
    }

    "fail to read when payload is empty" in {
      val json = Json.obj(
        "dataType" -> 1,
        "payload"  -> Json.obj()
      )

      val result = json.validate[EnqueueNumber]

      result mustBe a[JsError]
    }
  }
}
