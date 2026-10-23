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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.payments.ElectronicPaymentAllocationDetails
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.payments.ElectronicPaymentAllocationDetailsStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class ElectronicPaymentAllocationDetailsRepositoryISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class ElectronicPaymentAllocationDetailsRepositoryStub extends ElectronicPaymentAllocationDetailsRepository {

    override def getPayAllocationElectronicDtls(taxRef: Long, accPeriod: Long, payslipId: Long): Future[ElectronicPaymentAllocationDetails] =
      Future.successful(ElectronicPaymentAllocationDetailsStubData.getPayAllocationElectronicDtls(taxRef: Long, accPeriod: Long, payslipId: Long))
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[ElectronicPaymentAllocationDetailsRepository].toInstance(new ElectronicPaymentAllocationDetailsRepositoryStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[ElectronicPaymentAllocationDetailsRepository]

  "getPayAllocationElectronicDtls" should {

    "return electronic payment allocation details" in {

      val result = repo.getPayAllocationElectronicDtls(10L, 2L, 3L).futureValue

      result mustBe ElectronicPaymentAllocationDetailsStubData.ePaymentAllocationDetails

    }

    "return multiple electronic payment allocation details" in {

      val result = repo.getPayAllocationElectronicDtls(20L, 2L, 3L).futureValue

      result mustBe ElectronicPaymentAllocationDetailsStubData.multipleEPaymentAllocationDetails
    }

    "return empty electronic payment allocation details" in {

      val result = repo.getPayAllocationElectronicDtls(30L, 2L, 3L).futureValue

      result mustBe ElectronicPaymentAllocationDetailsStubData.emptyEPaymentAllocationDetails

    }

    "return downstream failure from stub" in {
      val exception = intercept[RuntimeException] {

        repo.getPayAllocationElectronicDtls(200L, 2L, 3L).futureValue
      }

      exception.getMessage must include("Downstream error")
    }

  }

}
