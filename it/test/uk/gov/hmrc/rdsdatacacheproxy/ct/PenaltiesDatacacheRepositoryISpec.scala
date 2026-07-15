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

package uk.gov.hmrc.rdsdatacacheproxy.ct

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.PenaltyTransaction
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.PenaltiesDatacacheRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.CorporationTaxStubData
import uk.gov.hmrc.rdsdatacacheproxy.gambling.models.{Assessments, Regime}
import uk.gov.hmrc.rdsdatacacheproxy.gambling.repositories.AssessmentsDataSource
import uk.gov.hmrc.rdsdatacacheproxy.gambling.stub.AssessmentsStubData.getAssessmentsData

import scala.concurrent.Future

class PenaltiesDatacacheRepositoryISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneAppPerSuite {

  class CorporationTaxDatacacheRepositoryRdsStub extends PenaltiesDatacacheRepository {
    def getPenaltyTransactionList(taxRef: Long, accPeriod: Long): Future[List[PenaltyTransaction]] =
      Future.successful(CorporationTaxStubData.getPenaltiesItems(taxRef))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[PenaltiesDatacacheRepository].toInstance(new CorporationTaxDatacacheRepositoryRdsStub))
    .build()

  private lazy val repository: PenaltiesDatacacheRepository = app.injector.instanceOf[PenaltiesDatacacheRepository]

  "getPenaltyTransactionList" should {

    "return correct Penalties list with two items" in {
      val result = repository.getPenaltyTransactionList(1L, 17L).futureValue

      result mustBe CorporationTaxStubData.penaltiesItems
    }

    "return correct Penalties list with no items" in {
      val result = repository.getPenaltyTransactionList(29L, 17L).futureValue

      result mustBe CorporationTaxStubData.penaltiesEmptyList
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[Error] {
        repository.getPenaltyTransactionList(19L, 29L).futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }
  }

}