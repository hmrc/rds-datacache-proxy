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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers.gpa

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.PaymentAllocationDetails
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa.PaymentAllocationDetailsRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.gpa.PaymentAllocationDetailsStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.Future

class PaymentAllocationDetailsControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class PaymentAllocationDetailsRepositoryStub extends PaymentAllocationDetailsRepository {
    override def getGPAPaymentAllocationDetail(gpaUtr: Long, gppContractVersion: Long, participatorUtr: Long, participatorAp: Long, startIndex: Long, count: Long): Future[PaymentAllocationDetails] = {
      Future.successful(PaymentAllocationDetailsStubData.getGPAPaymentAllocationDetail(gpaUtr: Long, gppContractVersion: Long, participatorUtr: Long, participatorAp: Long, startIndex: Long, count: Long))
    }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[PaymentAllocationDetailsRepository].toInstance(new PaymentAllocationDetailsRepositoryStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET /corporation-tax/gpa-payment-allocation-details" should {

    "return 200 and payment allocation details" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/gpa-payment-allocation-details/10/2/3/4/5/6").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PaymentAllocationDetails] mustBe PaymentAllocationDetailsStubData.fullPaymentAllocationDetails
    }

    "return 200 and payment allocation details with multiple allocation details" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/gpa-payment-allocation-details/20/2/3/4/5/6").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PaymentAllocationDetails] mustBe PaymentAllocationDetailsStubData.paymentAllocationDetailsWithMultipleAllocationDetails
    }

    "return 200 and payment allocation details with minimal details" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/gpa-payment-allocation-details/30/2/3/4/5/6").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[PaymentAllocationDetails] mustBe PaymentAllocationDetailsStubData.minimalPaymentAllocationDetails
    }

    "return 500 when stub fails" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/gpa-payment-allocation-details/200/2/3/4/5/6").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()

      val response = get(s"$endpoint/gpa-payment-allocation-details/40/2/3/4/5/6").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }

}
