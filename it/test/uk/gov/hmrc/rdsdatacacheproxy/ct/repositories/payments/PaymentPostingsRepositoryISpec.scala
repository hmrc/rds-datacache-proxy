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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.payments

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.PaymentPostingResponse
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.payments.PaymentPostingsStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PaymentPostingsRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with ApplicationWithWiremock
    with PaymentPostingsStubData {

  class PaymentPostingsRepositoryStub extends PaymentPostingsRepository {
    override def getPaymentPostings(taxRef: Long): Future[PaymentPostingResponse] =
      Future {
        paymentPostingsResponse
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[PaymentPostingsRepository].toInstance(new PaymentPostingsRepositoryStub)
      )
      .build()

  private val endpoint = "/corporation-tax"

  "GET /payment-postings/:taxRef" should {

    "return 200 with Payment Postings Response with a list containing multiple items" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/payment-postings/122323").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PaymentPostingResponse] mustBe paymentPostingsResponse
    }

    "return 200 with RdsAccountingPeriod empty list" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/payment-postings/122323").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PaymentPostingResponse] mustBe paymentPostingsResponseWIthEmptyList

    }

    "return 500 when stub simulates failure" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/payment-postings/122323").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val response = get(s"$endpoint/payment-postings/122323").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }

}
