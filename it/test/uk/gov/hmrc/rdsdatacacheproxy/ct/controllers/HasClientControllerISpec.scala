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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, inject}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.HasClient
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.HasClientRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.HasClientStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class HasClientControllerISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock {

  class HasClientStub extends HasClientRepository {
    override def hasClient(
      utr: Long,
      irAgentId: String,
      credentialId: String
    ): Future[HasClient] =
      Future {
        HasClientStubData.getHasClient(utr, irAgentId, credentialId)
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[HasClientRepository].toInstance(new HasClientStub)
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /corporation-tax/has-client/:utr/:irAgentId/:credentialId" should {

    "return 200 when client exists" in {
      AuthStub.authorised()

      val response = get(
        s"$endpoint/has-client/1/AGENT123/CREDENTIAL123"
      ).futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"
      response.json.as[HasClient] mustBe HasClient(status = 1)
    }

    "return 200 when client does not exist" in {
      AuthStub.authorised()

      val response = get(
        s"$endpoint/has-client/2/AGENT123/CREDENTIAL123"
      ).futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"
      response.json.as[HasClient] mustBe HasClient(status = 0)
    }
  }

  "return 400 when UTR is invalid" in {
    AuthStub.authorised()

    val response = get(s"$endpoint/has-client/invalid/AGENT123/CREDENTIAL123").futureValue

    response.status mustBe BAD_REQUEST
  }

  "return 401 when unauthorised" in {
    AuthStub.unauthorised()

    val response = get(s"$endpoint/has-client/1/AGENT123/CREDENTIAL123").futureValue
    response.status mustBe UNAUTHORIZED
  }

  "return 500 when stub simulates a downstream error" in {
    AuthStub.authorised()

    val response = get(s"$endpoint/has-client/67/AGENT123/CREDENTIAL123").futureValue
    response.status mustBe INTERNAL_SERVER_ERROR
  }
}
