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
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CompanyNominator
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.CompanyNominatorStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.Future

class CompanyNominatorRepositoryISpec
  extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class CompanyNominatorRepositoryStub extends CompanyNominatorRepository {

    override def getIsCompanyNominatorOfGPA(gpaUtr: Long, nominatedCompanyUtr: Long): Future[CompanyNominator] =
      Future.successful(CompanyNominatorStubData.getIsCompanyNominatorOfGPA(gpaUtr: Long, nominatedCompanyUtr: Long))
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[CompanyNominatorRepository].toInstance(new CompanyNominatorRepositoryStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[CompanyNominatorRepository]

  "getIsCompanyNominatorOfGPA" should {

    "return company nominator when isParticipator is 'Y'" in {

      val result = repo.getIsCompanyNominatorOfGPA(10L, 2L).futureValue

      result mustBe CompanyNominatorStubData.companyNominatorTrue

    }

    "return company nominator when isParticipator is 'N'" in {

      val result = repo.getIsCompanyNominatorOfGPA(20L, 2L).futureValue

      result mustBe CompanyNominatorStubData.companyNominatorFalse
    }
    

    "return downstream failure from stub" in {
      val exception = intercept[RuntimeException] {

        repo.getIsCompanyNominatorOfGPA(200L, 2L).futureValue
      }

      exception.getMessage must include("Downstream error")
    }

  }

}
