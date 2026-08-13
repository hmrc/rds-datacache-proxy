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

package uk.gov.hmrc.rdsdatacacheproxy.ct.repositories

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.ReallocationRow
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.CorporationTaxStubData

import scala.concurrent.Future

class ReallocationDatacacheRepositoryISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneAppPerSuite {

  class ReallocationDatacacheRepositoryRdsStub extends ReallocationDatacacheRepository {
    def getByAccountingPeriod(taxRef: Long, accPeriod: Long): Future[Seq[ReallocationRow]] =
      Future.successful(CorporationTaxStubData.getReallocations(taxRef, accPeriod))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[ReallocationDatacacheRepository].toInstance(new ReallocationDatacacheRepositoryRdsStub))
    .build()

  private lazy val repository: ReallocationDatacacheRepository = app.injector.instanceOf[ReallocationDatacacheRepository]

  "getByAccountingPeriod" should {

    "return correct ReallocationItem list with two items" in {
      val result = repository.getByAccountingPeriod(2L, 17L).futureValue

      result mustBe CorporationTaxStubData.reallocationsTwoItems
    }

    "return correct Penalties list with no items" in {
      val result = repository.getByAccountingPeriod(0L, 17L).futureValue

      result mustBe CorporationTaxStubData.reallocationsEmpty
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[Error] {
        repository.getByAccountingPeriod(3L, 29L).futureValue
      }

      exception.getMessage must include("Simulated downstream failure")
    }

  }

}
