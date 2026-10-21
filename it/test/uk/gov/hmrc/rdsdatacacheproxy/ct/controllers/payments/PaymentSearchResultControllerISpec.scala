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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.PaymentSearchResultStubData
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.PaymentSearchResultResponse
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments.PaymentSearchResultRepository
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}


class PaymentSearchResultControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock with PaymentSearchResultStubData {


  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[PaymentSearchResultRepository].toInstance(new PaymentSearchResultRdsStub())
      )
      .build()


   private final val endpoint = "/corporation-tax"


  "GET /corporation-tax/payment-search-result" should {

    "return 200 with PaymentSearchResultResponse list contains multiple items" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/payment-search-result/20").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PaymentSearchResultResponse] mustBe multiplePaymentSearchResultResponse
    }

    "return 200 with PaymentSearchResultResponse single list" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/payment-search-result/10").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PaymentSearchResultResponse] mustBe singlePaymentSearchResultResponse

    }

    "return 200 with PaymentSearchResultResponse single list and valid query params" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/payment-search-result/10?fromDate=2006-01-01&endDate=2007-01-01").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PaymentSearchResultResponse] mustBe singlePaymentSearchResultResponse

    }

    "return 200 with empty record" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/payment-search-result/2").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PaymentSearchResultResponse] mustBe emptyPaymentSearchResultResponse

    }

    "return 400 when incorrect query params entered " in {

      AuthStub.authorised()

      val response = get(s"$endpoint/payment-search-result/10?fromDate=2006-01-&endDate=2007-01-01").futureValue

      response.status mustBe BAD_REQUEST

    }

    "return 500 when stub simulates failure" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/payment-search-result/200").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val response = get(s"$endpoint/payment-search-result/3").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }


}
