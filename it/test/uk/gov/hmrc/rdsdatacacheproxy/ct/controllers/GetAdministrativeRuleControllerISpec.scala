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

package uk.gov.hmrc.rdsdatacacheproxy.ct.controllers

import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.AdminRule
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.AdministrativeRuleRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.AdministrativeRuleStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GetAdministrativeRuleControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class AdministrativeRuleStub extends AdministrativeRuleRepository {
    override def getAdminRule(adminRuleKey: String): Future[AdminRule] =
      Future {
        AdministrativeRuleStubData.getAdminRule(adminRuleKey)
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[AdministrativeRuleRepository].toInstance(new AdministrativeRuleStub)
      )
      .build()

  private val endpoint = "/corporation-tax"

  "GET /corporation-tax/administrative-rule" should {

    "return 200 with AdminRule with all fields " in {
      AuthStub.authorised()

      val adminRule:String = "START-OF-CTSA"

      val response = get(s"$endpoint/administrative-rule/$adminRule").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[AdminRule] mustBe AdministrativeRuleStubData.adminRuleWithAllFields
    }
    "return 200 with AdminRule with only ruleNumber " in {
      AuthStub.authorised()

      val adminRule: String = "INST-REV-PER-D"

      val response = get(s"$endpoint/administrative-rule/$adminRule").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[AdminRule] mustBe AdministrativeRuleStubData.adminRuleWithoutRuleDate
    }
    "return 200 with AdminRule with only ruleDate " in {
      AuthStub.authorised()

      val adminRule: String = "LAST-INST-PER-M"

      val response = get(s"$endpoint/administrative-rule/$adminRule").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[AdminRule] mustBe AdministrativeRuleStubData.adminRuleWithoutRuleNumber
    }

    "return 200 with empty AdminRule" in {

      AuthStub.authorised()

      val adminRule: String = "INST-PERIOD"

      val response = get(s"$endpoint/administrative-rule/$adminRule").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[AdminRule] mustBe AdministrativeRuleStubData.adminRuleWithEmptyFields

    }

    "return 500 when stub simulates failure" in {

      AuthStub.authorised()

      val invalidAdminRule = "invalidAdminRule"

      val response = get(s"$endpoint/administrative-rule/$invalidAdminRule").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val adminRule: String = "INST-REV-PER-D"

      val response = get(s"$endpoint/administrative-rule/$adminRule").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }

}
