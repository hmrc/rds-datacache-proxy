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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.payments

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, inject}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.PostingSummaryByTypeResponse
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments.PostingSummaryByTypeRepository
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.payments.PostingSummaryByTypeStubData

import scala.concurrent.Future


class PostingSummaryByTypeControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock with PostingSummaryByTypeStubData {

  class PostingSummaryByTypeRdsStubData extends PostingSummaryByTypeRepository {

    override def getPostingSummaryByType(taxRef: Long, postingType: String): Future[PostingSummaryByTypeResponse] = {
      Future.successful(
        getPostingSummaryByPostingTypeData(taxRef, postingType)
      )
    }

  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[PostingSummaryByTypeRepository].toInstance(new PostingSummaryByTypeRdsStubData())
      )
      .build()


   private final val endpoint = "/corporation-tax/posting-summary"


  "GET /corporation-tax/posting-summary" should {

    "return 200 with PostingSummaryByTypeResponse list contains multiple items" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/1/PAF").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PostingSummaryByTypeResponse] mustBe getPostingSummaryByPostingTypeData(1, "PAF")
    }

    "return 200 with PostingSummaryByTypeResponse with missing fields" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/2/PYT").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PostingSummaryByTypeResponse] mustBe postingSummaryByTypeRowMultipleMissingFields

    }

    "return 200 with empty record" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/4/ABC").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PostingSummaryByTypeResponse] mustBe emptyPostingSummaryByTypeResponses

    }

    "return 400 when incorrect query params entered " in {

      AuthStub.authorised()

      val response = get(s"$endpoint/a/123").futureValue

      response.status mustBe BAD_REQUEST

    }

    "return 500 when stub simulates failure" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/3/SAM").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val response = get(s"$endpoint/3/PAF").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }


}
