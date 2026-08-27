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
import play.api.libs.json.Json

class EnqueueMessageRequestSpec extends AnyFreeSpec with Matchers {

  "EnqueueMessageRequest JSON format" - {

    "read from JSON correctly without tracking" in {
      val json = Json.obj(
        "message" -> Json.obj(
          "sender"        -> "Portal",
          "queueName"     -> "AGTAUTH",
          "replyQueue"    -> "",
          "correlationID" -> "",
          "filter"        -> "UpdateAgentOwnReference",
          "payload" -> Json.obj(
            "IRAgentID"    -> "123456789",
            "Service"      -> "CIS",
            "TaxReference" -> "123/ABC123"
          )
        )
      )

      val result = json.as[EnqueueMessageRequest]

      result mustBe EnqueueMessageRequest(
        message = EnqueueMessage(
          sender        = "Portal",
          queueName     = "AGTAUTH",
          replyQueue    = "",
          correlationID = "",
          filter        = "UpdateAgentOwnReference",
          payload = Map(
            "IRAgentID"    -> "123456789",
            "Service"      -> "CIS",
            "TaxReference" -> "123/ABC123"
          )
        )
      )
    }

    "write to JSON correctly without tracking" in {
      val model = EnqueueMessageRequest(
        message = EnqueueMessage(
          sender        = "Portal",
          queueName     = "AGTAUTH",
          replyQueue    = "",
          correlationID = "",
          filter        = "UpdateAgentOwnReference",
          payload = Map(
            "IRAgentID"    -> "123456789",
            "Service"      -> "CIS",
            "TaxReference" -> "123/ABC123"
          )
        )
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "message" -> Json.obj(
          "sender"        -> "Portal",
          "queueName"     -> "AGTAUTH",
          "replyQueue"    -> "",
          "correlationID" -> "",
          "filter"        -> "UpdateAgentOwnReference",
          "payload" -> Json.obj(
            "IRAgentID"    -> "123456789",
            "Service"      -> "CIS",
            "TaxReference" -> "123/ABC123"
          )
        )
      )
    }

    "read from JSON correctly with tracking" in {
      val json = Json.obj(
        "message" -> Json.obj(
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
        ),
        "tracking" -> Json.obj(
          "message" -> Json.obj(
            "sender"        -> "Portal",
            "queueName"     -> "Tracking",
            "replyQueue"    -> "",
            "correlationID" -> "",
            "filter"        -> "AGENTAUTH",
            "payload" -> Json.obj(
              "GGIS_DTSTAMP"    -> "20260826",
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
      )

      val result = json.as[EnqueueMessageRequest]

      result mustBe EnqueueMessageRequest(
        message = EnqueueMessage(
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
        ),
        tracking = Some(
          EnqueueTracking(
            message = EnqueueMessage(
              sender        = "Portal",
              queueName     = "Tracking",
              replyQueue    = "",
              correlationID = "",
              filter        = "AGENTAUTH",
              payload = Map(
                "GGIS_DTSTAMP"    -> "20260826",
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
        )
      )
    }

    "write to JSON correctly with tracking" in {
      val model = EnqueueMessageRequest(
        message = EnqueueMessage(
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
        ),
        tracking = Some(
          EnqueueTracking(
            message = EnqueueMessage(
              sender        = "Portal",
              queueName     = "Tracking",
              replyQueue    = "",
              correlationID = "",
              filter        = "AGENTAUTH",
              payload = Map(
                "GGIS_DTSTAMP"    -> "20260826",
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
        )
      )

      val json = Json.toJson(model)

      json mustBe Json.obj(
        "message" -> Json.obj(
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
        ),
        "tracking" -> Json.obj(
          "message" -> Json.obj(
            "sender"        -> "Portal",
            "queueName"     -> "Tracking",
            "replyQueue"    -> "",
            "correlationID" -> "",
            "filter"        -> "AGENTAUTH",
            "payload" -> Json.obj(
              "GGIS_DTSTAMP"    -> "20260826",
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
      )
    }
  }
}
