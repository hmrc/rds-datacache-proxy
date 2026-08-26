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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK, UNAUTHORIZED, BAD_REQUEST}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.StatuteRuleDataStub
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.StatuteRule
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.StatuteRuleRepository
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}


class StatuteRuleControllerISpec extends AnyWordSpec with Matchers
  with ScalaFutures with IntegrationPatience with ApplicationWithWiremock
  with StatuteRuleDataStub {

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[StatuteRuleRepository].toInstance(new StatuteRuleRdsStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"

  "GET ~/statute-rule" should {

    "return 404 with empty Statute record" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/statute-rule/C1/1991-01-19/1992-02-20").futureValue

      response.status mustBe NOT_FOUND
      response.contentType mustBe "application/json"

      response.json.as[StatuteRule] mustBe StatuteRule(None)
    }

    "return 200 with Statute record with empty values" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/statute-rule?ruleKey=C&startDate=1991-01-19&endDate=1992-02-20").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[StatuteRule] mustBe StatuteRule(Some(recordWithEmptyFields))
    }

    "return 200 with Statute default record" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/statute-rule?ruleKey=C&startDate=1991-04-19&endDate=1992-06-20").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[StatuteRule] mustBe StatuteRule(Some(defaultRecord))
    }

    "return 500 when stub simulates failure" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/statute-rule?ruleKey=C5&startDate=1990-04-19&endDate=1990-06-20").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 500 when incorret dates / query params" in {
      AuthStub.authorised()
      val response = get(s"$endpoint/statute-rule?ruleKey=C5&startDate=1990-04-&endDate=1990-06-2").futureValue

      response.status mustBe BAD_REQUEST
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/statute-rule?ruleKey=C&startDate=1991-04-19&endDate=1992-06-20").futureValue
      response.status mustBe UNAUTHORIZED
    }

  }

}
