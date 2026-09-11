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

    "return 204 with messageIDOut when authorised and JSON is valid" in {

      AuthStub.authorised()
      val res = postJson(endpoint, validJson)

      res.status mustBe OK
      (res.json \ "messageIDOut").as[Long] mustBe 10L
    }

    "return 400 when JSON is missing required fields" in {

      AuthStub.authorised()

      val invalidJsonWithoutSender = Json.obj(
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

      val res1 = postJson(endpoint, invalidJsonWithoutSender)
      res1.status mustBe BAD_REQUEST
      (res1.json \ "message").as[String].toLowerCase must include("invalid json")

      val invalidJsonWithoutQueueName = Json.obj(
        "sender"        -> "Portal",
        "replyQueue"    -> "",
        "correlationID" -> "",
        "filter"        -> "RemoveClient",
        "payload" -> Json.obj(
          "IRAgentID"    -> "123456789",
          "Service"      -> "CIS",
          "TaxReference" -> "123/ABC123"
        )
      )

      val res2 = postJson(endpoint, invalidJsonWithoutQueueName)
      res2.status mustBe BAD_REQUEST
      (res2.json \ "message").as[String].toLowerCase must include("invalid json")

      val invalidJsonWithoutReplyQueue = Json.obj(
        "sender"        -> "Portal",
        "queueName"     -> "AGTAUTH",
        "correlationID" -> "",
        "filter"        -> "RemoveClient",
        "payload" -> Json.obj(
          "IRAgentID"    -> "123456789",
          "Service"      -> "CIS",
          "TaxReference" -> "123/ABC123"
        )
      )

      val res3 = postJson(endpoint, invalidJsonWithoutReplyQueue)
      res3.status mustBe BAD_REQUEST
      (res3.json \ "message").as[String].toLowerCase must include("invalid json")

      val invalidJsonWithoutCorrelationID = Json.obj(
        "sender"     -> "Portal",
        "queueName"  -> "AGTAUTH",
        "replyQueue" -> "",
        "filter"     -> "RemoveClient",
        "payload" -> Json.obj(
          "IRAgentID"    -> "123456789",
          "Service"      -> "CIS",
          "TaxReference" -> "123/ABC123"
        )
      )

      val res4 = postJson(endpoint, invalidJsonWithoutCorrelationID)
      res4.status mustBe BAD_REQUEST
      (res4.json \ "message").as[String].toLowerCase must include("invalid json")

      val invalidJsonWithoutFilter = Json.obj(
        "sender"        -> "Portal",
        "queueName"     -> "AGTAUTH",
        "replyQueue"    -> "",
        "correlationID" -> "",
        "payload" -> Json.obj(
          "IRAgentID"    -> "123456789",
          "Service"      -> "CIS",
          "TaxReference" -> "123/ABC123"
        )
      )

      val res5 = postJson(endpoint, invalidJsonWithoutFilter)
      res5.status mustBe BAD_REQUEST
      (res5.json \ "message").as[String].toLowerCase must include("invalid json")

      val invalidJsonWithoutPayload = Json.obj(
        "sender"        -> "Portal",
        "queueName"     -> "AGTAUTH",
        "replyQueue"    -> "",
        "correlationID" -> "",
        "filter"        -> "RemoveClient"
      )

      val res6 = postJson(endpoint, invalidJsonWithoutPayload)
      res6.status mustBe BAD_REQUEST
      (res6.json \ "message").as[String].toLowerCase must include("invalid json")
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
