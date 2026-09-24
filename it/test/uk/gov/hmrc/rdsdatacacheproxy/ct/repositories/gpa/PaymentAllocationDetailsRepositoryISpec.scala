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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.PaymentAllocationDetails
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.gpa.PaymentAllocationDetailsStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class PaymentAllocationDetailsRepositoryISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class PaymentAllocationDetailsRepositoryStub extends PaymentAllocationDetailsRepository {

    override def getGPAPaymentAllocationDetail(gpaUtr: Long, gppContractVersion: Long, participatorUtr: Long, participatorAp: Long, startIndex: Long, count: Long): Future[PaymentAllocationDetails] =
      Future.successful(PaymentAllocationDetailsStubData.getGPAPaymentAllocationDetail(gpaUtr: Long, gppContractVersion: Long, participatorUtr: Long, participatorAp: Long, startIndex: Long, count: Long))
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[PaymentAllocationDetailsRepository].toInstance(new PaymentAllocationDetailsRepositoryStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[PaymentAllocationDetailsRepository]

  "getGPAPaymentAllocationDetail" should {

    "return payment allocation details" in {

      val result = repo.getGPAPaymentAllocationDetail(10L, 2L, 3L, 4L, 5L, 6L).futureValue

      result mustBe PaymentAllocationDetailsStubData.fullPaymentAllocationDetails

    }

    "return payment allocation details with multiple allocation details" in {

      val result = repo.getGPAPaymentAllocationDetail(20L, 2L, 3L, 4L, 5L, 6L).futureValue

      result mustBe PaymentAllocationDetailsStubData.paymentAllocationDetailsWithMultipleAllocationDetails
    }

    "return payment allocation details with minimal details" in {

      val result = repo.getGPAPaymentAllocationDetail(30L, 2L, 3L, 4L, 5L, 6L).futureValue

      result mustBe PaymentAllocationDetailsStubData.minimalPaymentAllocationDetails

    }

    "return downstream failure from stub" in {
      val exception = intercept[RuntimeException] {

        repo.getGPAPaymentAllocationDetail(200L, 2L, 3L, 4L, 5L, 6L).futureValue
      }

      exception.getMessage must include("Downstream error")
    }

  }

}
