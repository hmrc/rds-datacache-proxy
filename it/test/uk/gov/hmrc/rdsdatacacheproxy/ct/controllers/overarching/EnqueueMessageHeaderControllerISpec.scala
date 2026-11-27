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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.overarching

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.{Application, inject}
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.overarching.{Enqueue, EnqueueMessageHeaderRequest}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.overarching.EnqueueMessageHeaderRepository
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.overarching.EnqueueMessageHeaderStubData

import scala.concurrent.Future

class EnqueueMessageHeaderControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  private val endpoint = "/corporation-tax/enqueue-message-header"

  class EnqueueMessageHeaderStub extends EnqueueMessageHeaderRepository {
    override def enqueueMessageHeader(request: EnqueueMessageHeaderRequest): Future[Enqueue] = {
      Future.successful(EnqueueMessageHeaderStubData.getEnqueue(request: EnqueueMessageHeaderRequest))
    }
  }


  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        inject.bind[EnqueueMessageHeaderRepository].toInstance(new EnqueueMessageHeaderStub())
      )
      .build()


  "POST /enqueue-message-header (stubbed repo, no DB)" should {

    val validJson = Json.obj(
        "sender"        -> "Portal",
        "queue"     -> "AGTAUTH",
        "replyQueue"    -> "",
        "correlationId" -> "",
        "filter"        -> "RemoveClient"
    )

    "return 200 with messageIDOut when authorised and JSON is valid" in {

      AuthStub.authorised()
      val response = post(endpoint, validJson).futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[Enqueue] mustBe EnqueueMessageHeaderStubData.enqueue
    }

    "return 400 when JSON is missing required fields" in {

      AuthStub.authorised()

      val missingFieldJson = Json.obj(
        "sender" -> "Portal",
        "queue" -> "AGTAUTH",
        "replyQueue" -> "",
        "correlationId" -> ""
      )

      val res1 = post(endpoint, missingFieldJson).futureValue
      res1.status mustBe BAD_REQUEST
      (res1.json \ "message").as[String].toLowerCase must include("invalid json")
    }

    "return 401 when there is no active session" in {
      AuthStub.unauthorised()
      val res = post(endpoint, validJson).futureValue

      res.status mustBe UNAUTHORIZED
    }

    "return 404 for unknown endpoint" in {
      AuthStub.authorised()
      val res = post("/does-not-exist", validJson).futureValue
      res.status mustBe NOT_FOUND
    }
  }
}
