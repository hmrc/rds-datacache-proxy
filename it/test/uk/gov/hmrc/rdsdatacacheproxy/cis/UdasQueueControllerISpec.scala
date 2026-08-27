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

package uk.gov.hmrc.rdsdatacacheproxy.cis

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.*
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.WSResponse
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

class UdasQueueControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  private val endpoint = "/cis/enqueue-message"

  private def postJson(uri: String, body: JsValue): WSResponse =
    post(uri, body).futureValue

  "POST /enqueue-message (stubbed repo, no DB)" should {

    val validJson = Json.obj(
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
    )

    "return 200 with messageIDOut when authorised and JSON is valid" in {

      AuthStub.authorised()
      val res = postJson(endpoint, validJson)

      res.status mustBe OK
      (res.json \ "messageIDOut").as[Long] mustBe 10L
    }

    "return 400 when JSON is missing required fields" in {

      AuthStub.authorised()

      val invalidJson = Json.obj(
        "message" -> Json.obj(
          "sender"        -> "Portal",
          "queueName"     -> "AGTAUTH",
          "replyQueue"    -> "",
          "correlationID" -> "",
          // "filter"        -> "RemoveClient", to make it invalid
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
      )

      val res1 = postJson(endpoint, invalidJson)
      res1.status mustBe BAD_REQUEST
      (res1.json \ "message").as[String].toLowerCase must include("invalid json")
    }

    "return 401 when there is no active session" in {
      AuthStub.unauthorised()
      val res = postJson(endpoint, validJson)

      res.status mustBe UNAUTHORIZED
    }

    "return 404 for unknown endpoint (routing sanity)" in {
      AuthStub.authorised()
      val res = postJson("/does-not-exist", validJson)
      res.status mustBe NOT_FOUND
    }
  }
}
