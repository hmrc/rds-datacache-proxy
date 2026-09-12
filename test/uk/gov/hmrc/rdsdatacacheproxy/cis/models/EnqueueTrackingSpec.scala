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
import play.api.libs.json.Json

class EnqueueTrackingSpec extends AnyFreeSpec with Matchers {

  "EnqueueTracking JSON format" - {

    "read from JSON correctly" in {
      val json = Json.obj(
        "message" -> Json.obj(
          "sender"        -> "Portal",
          "queueName"     -> "Tracking",
          "replyQueue"    -> "",
          "correlationID" -> "",
          "filter"        -> "AGENTAUTH",
          "payload" -> Json.obj(
            "GGIS_DTSTAMP"    -> "20260827 154512747",
            "MESSAGE_TYPE"    -> "AGENT_AUTH_PORTAL",
            "ADDITIONAL_INFO" -> "Request client removal",
            "GW_AGENT_ID"     -> "AGENT123",
            "IR_CLIENT_REF"   -> "123/ABC123",
            "USER_ID"         -> "user123",
            "Service"         -> "CIS"
          )
        ),
        "number" -> Json.obj(
          "dataType" -> 1,
          "payload" -> Json.obj(
            "EVENT_TYPE" -> 1010L
          )
        )
      )

      val result = json.as[EnqueueTracking]

      result mustBe EnqueueTracking(
        message = EnqueueMessage(
          sender        = "Portal",
          queueName     = "Tracking",
          replyQueue    = "",
          correlationID = "",
          filter        = "AGENTAUTH",
          payload = Map(
            "GGIS_DTSTAMP"    -> "20260827 154512747",
            "MESSAGE_TYPE"    -> "AGENT_AUTH_PORTAL",
            "ADDITIONAL_INFO" -> "Request client removal",
            "GW_AGENT_ID"     -> "AGENT123",
            "IR_CLIENT_REF"   -> "123/ABC123",
            "USER_ID"         -> "user123",
            "Service"         -> "CIS"
          )
        ),
        number = EnqueueNumber(
          dataType = 1,
          payload = Map(
            "EVENT_TYPE" -> 1010L
          )
        )
      )
    }

    "write to JSON correctly" in {
      val model = EnqueueTracking(
        message = EnqueueMessage(
          sender        = "Portal",
          queueName     = "Tracking",
          replyQueue    = "",
          correlationID = "",
          filter        = "AGENTAUTH",
          payload = Map(
            "GGIS_DTSTAMP"    -> "20260827 154512747",
            "MESSAGE_TYPE"    -> "AGENT_AUTH_PORTAL",
            "ADDITIONAL_INFO" -> "Request client removal",
            "GW_AGENT_ID"     -> "AGENT123",
            "IR_CLIENT_REF"   -> "123/ABC123",
            "USER_ID"         -> "user123",
            "Service"         -> "CIS"
          )
        ),
        number = EnqueueNumber(
          dataType = 1,
          payload = Map(
            "EVENT_TYPE" -> 1010L
          )
        )
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "message" -> Json.obj(
          "sender"        -> "Portal",
          "queueName"     -> "Tracking",
          "replyQueue"    -> "",
          "correlationID" -> "",
          "filter"        -> "AGENTAUTH",
          "payload" -> Json.obj(
            "GGIS_DTSTAMP"    -> "20260827 154512747",
            "MESSAGE_TYPE"    -> "AGENT_AUTH_PORTAL",
            "ADDITIONAL_INFO" -> "Request client removal",
            "GW_AGENT_ID"     -> "AGENT123",
            "IR_CLIENT_REF"   -> "123/ABC123",
            "USER_ID"         -> "user123",
            "Service"         -> "CIS"
          )
        ),
        "number" -> Json.obj(
          "dataType" -> 1,
          "payload" -> Json.obj(
            "EVENT_TYPE" -> 1010L
          )
        )
      )
    }
  }
}
