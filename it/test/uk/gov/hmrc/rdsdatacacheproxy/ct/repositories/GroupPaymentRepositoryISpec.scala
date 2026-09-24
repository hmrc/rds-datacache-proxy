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
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.GroupPaymentsHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{GroupReferenceNumberLstItem, GroupSummaryDetails}


class GroupPaymentRepositoryISpec extends AnyWordSpec
  with Matchers with ScalaFutures with IntegrationPatience
  with GuiceOneAppPerSuite
  with GroupPaymentsHelper {


  override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[GroupPaymentsRepository].toInstance(new GroupPaymentsRepositoryDataSource))
    .build()

  private lazy val repository: GroupPaymentsRepository = app.injector.instanceOf[GroupPaymentsRepository]

  "getGroupSummary" should {


    "return correct Group Payment record" in {
      val result = repository.getGroupSummary(1L, 1L).futureValue

      result mustBe Some(GroupSummaryDetails(
        gpaGrpSummaryDetails = List(
          groupSummaryDetItemOne
        ),
        gpaReferenceNumberLst = List(
          GroupReferenceNumberLstItem(112)
        ),
        nominatedCompanyName = "Some company name"
      ))
    }

    "return correct Group Payment record with empty refs" in {
      val result = repository.getGroupSummary(3L, 17L).futureValue

      result mustBe Some(GroupSummaryDetails(
        gpaGrpSummaryDetails = List.empty,
        gpaReferenceNumberLst = List.empty,
        nominatedCompanyName = "Empty company name"
      )
      )
    }

    "propagate downstream failure from stub" in {
      val exception = intercept[Error] {
        repository.getGroupSummary(99L, 9L).futureValue
      }

      exception.getMessage must include("Boom")
    }

  }

}
