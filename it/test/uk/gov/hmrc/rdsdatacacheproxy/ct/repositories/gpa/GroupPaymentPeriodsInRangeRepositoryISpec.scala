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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa.PeriodWithinRangeHelper.{periodWithinRangeFalse, periodWithinRangeTrue}
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.gpa.PeriodWithinRangeHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.gpa.PeriodWithinRange
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.gpa.GroupPaymentPeriodsInRangeRepository

import scala.concurrent.Future

class GroupPaymentPeriodsInRangeRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite {

  class GroupPaymentPeriodsInRangeRepositoryStub extends GroupPaymentPeriodsInRangeRepository {
    override def getGroupPaymentPeriodsInRange(gpaUTR: Long,
                                                   nominatedCompanyUTR: Long,
                                                   pPeriod: Int,
                                                   pMonthRestriction: Int
                                                  ): Future[PeriodWithinRange] =
      Future.successful(PeriodWithinRangeHelper.getGroupPaymentPeriodsInRange(gpaUTR, nominatedCompanyUTR, pPeriod, pMonthRestriction))
  }

  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[GroupPaymentPeriodsInRangeRepository].toInstance(new GroupPaymentPeriodsInRangeRepositoryStub))
    .build()

  private lazy val repository: GroupPaymentPeriodsInRangeRepository = app.injector.instanceOf[GroupPaymentPeriodsInRangeRepository]

  "getGroupPaymentPeriodsInRange" should {

    "return PeriodWithinRange as No" in {
      val result = repository.getGroupPaymentPeriodsInRange(10L, 1000L, 1, 1).futureValue

      result mustBe periodWithinRangeFalse
    }

    "return PeriodWithinRange as Yes" in {
      val result = repository.getGroupPaymentPeriodsInRange(20L, 1000L, 1, 1).futureValue

      result mustBe periodWithinRangeTrue
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[RuntimeException] {
        repository.getGroupPaymentPeriodsInRange(999L, 1000L, 1, 1).futureValue
      }

      exception.getMessage must include("Error from downstream")
    }

  }

}
