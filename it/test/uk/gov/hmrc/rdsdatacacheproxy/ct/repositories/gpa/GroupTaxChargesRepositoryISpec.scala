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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa.GroupTaxChargesStubData
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.APBalancedItem
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.GpaGroupTaxCharges
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.AccountingPeriodDetailsStubData

import scala.concurrent.Future

class GroupTaxChargesRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with GroupTaxChargesStubData {

  class GroupTaxChargesRepositoryRdsStub extends GroupTaxChargesRepository {
    def getGPAGroupTaxCharges(pGpaUtr: Long, pGppContractVersion: Long, pStartIndex: Long, pCount: Long): Future[GpaGroupTaxCharges] =
      Future.successful(
        getGroupTaxCharges(pGpaUtr: Long, pGppContractVersion: Long, pStartIndex: Long, pCount: Long)
      )
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[GroupTaxChargesRepository].toInstance(new GroupTaxChargesRepositoryRdsStub))
    .build()

  private lazy val repository: GroupTaxChargesRepository = app.injector.instanceOf[GroupTaxChargesRepository]

  "getGPAGroupTaxCharges" should {

    "return correct GpaGroupTaxCharges with empty ParticipatorDetails" in {
      val result = repository.getGPAGroupTaxCharges(1L, 2L, 3L, 4L).futureValue

      result mustBe gpaWithEmptyParticipator
    }

    "return correct GpaGroupTaxCharges with Participator Details" in {
      val result = repository.getGPAGroupTaxCharges(12L, 13L, 14L, 15L).futureValue

      result mustBe gpaWithNonEmptyParticipator
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[Error] {
        repository.getGPAGroupTaxCharges(5L, 6L, 7L, 8L).futureValue
      }

      exception.getMessage must include("No Data found")
    }

  }

}
