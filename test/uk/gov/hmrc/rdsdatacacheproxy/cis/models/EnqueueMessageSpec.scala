/*
 * Copyright 2026 HM Revenue & Customs
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

class EnqueueMessageSpec extends AnyFreeSpec with Matchers {

  "EnqueueMessage JSON format" - {

    "read from JSON correctly" in {
      val json = Json.obj(
        "sender"        -> "Portal",
        "queueName"     -> "AGTAUTH",
        "replyQueue"    -> "",
        "correlationID" -> "",
        "filter"        -> "RemoveClient",
        "payload" -> Json.obj(
          "IRAgentID"    -> "123456789",
          "Service"      -> "CIS",
          "TaxReference" -> "123/ABC123"
        )
      )

      val result = json.as[EnqueueMessage]

      result mustBe EnqueueMessage(
        sender        = "Portal",
        queueName     = "AGTAUTH",
        replyQueue    = "",
        correlationID = "",
        filter        = "RemoveClient",
        payload = Map(
          "IRAgentID"    -> "123456789",
          "Service"      -> "CIS",
          "TaxReference" -> "123/ABC123"
        )
      )
    }

    "write to JSON correctly" in {
      val model = EnqueueMessage(
        sender        = "Portal",
        queueName     = "AGTAUTH",
        replyQueue    = "",
        correlationID = "",
        filter        = "RemoveClient",
        payload = Map(
          "IRAgentID"    -> "123456789",
          "Service"      -> "CIS",
          "TaxReference" -> "123/ABC123"
        )
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "sender"        -> "Portal",
        "queueName"     -> "AGTAUTH",
        "replyQueue"    -> "",
        "correlationID" -> "",
        "filter"        -> "RemoveClient",
        "payload" -> Json.obj(
          "IRAgentID"    -> "123456789",
          "Service"      -> "CIS",
          "TaxReference" -> "123/ABC123"
        )
      )
    }

    "fail to read when payload is empty" in {
      val json = Json.obj(
        "sender"        -> "Portal",
        "queueName"     -> "AGTAUTH",
        "replyQueue"    -> "",
        "correlationID" -> "",
        "filter"        -> "RemoveClient",
        "payload"       -> Json.obj()
      )

      val result = json.validate[EnqueueMessage]

      result mustBe a[JsError]
    }
  }
}
