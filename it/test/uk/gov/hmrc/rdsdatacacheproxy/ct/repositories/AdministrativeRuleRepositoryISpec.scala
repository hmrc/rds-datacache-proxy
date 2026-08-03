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
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import play.api.inject.bind
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.AdminRule
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.AdministrativeRuleStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.ApplicationWithWiremock

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AdministrativeRuleRepositoryISpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with ApplicationWithWiremock {

  class AdministrativeRuleRepositoryStub extends AdministrativeRuleRepository {
    override def getAdminRule(adminRuleKey: String): Future[AdminRule] =
      Future {
        AdministrativeRuleStubData.getAdminRule(adminRuleKey)
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[AdministrativeRuleRepository].toInstance(new AdministrativeRuleRepositoryStub)
      )
      .build()

  private lazy val repo = app.injector.instanceOf[AdministrativeRuleRepository]

  "getAdminRule" should {
    "return AdminRule with all fields " in {
      val result = repo.getAdminRule("START-OF-CTSA").futureValue

      result shouldBe AdministrativeRuleStubData.adminRuleWithAllFields
    }
    "return AdminRule with only ruleNumber " in {
      val result = repo.getAdminRule("INST-REV-PER-D").futureValue

      result shouldBe AdministrativeRuleStubData.adminRuleWithoutRuleDate
    }
    "return AdminRule with only ruleDate " in {
      val result = repo.getAdminRule("LAST-INST-PER-M").futureValue

      result shouldBe AdministrativeRuleStubData.adminRuleWithoutRuleNumber
    }
    "return empty AdminRule" in {
      val result = repo.getAdminRule("INST-PERIOD").futureValue

      result shouldBe AdministrativeRuleStubData.adminRuleWithEmptyFields
    }
    "return failure from downstream services" in {
      val exception = intercept[RuntimeException] {
        repo.getAdminRule("123").futureValue
      }

      exception.getMessage must include("Boom")
    }
  }

}
