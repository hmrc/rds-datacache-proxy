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
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.FormDataStub
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CT600XmlDataResponse
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.FormDataRepository
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}


class FormDataControllerISpec extends AnyWordSpec
  with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock
  with FormDataStub {

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[FormDataRepository].toInstance(new FormDataRdsStub())
      )
      .build()

  private final val endpoint = "/corporation-tax"


  "GET ~/ct-form-data" should {

    "return 200 with default record" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/ct-form-data/1/1?startDate=2006-01-01&endDate=2006-12-31").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[CT600XmlDataResponse] mustBe defaultDataItem
    }


    "return 200 with empty record" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/ct-form-data/5/5?startDate=2006-01-01&endDate=2006-12-31").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[CT600XmlDataResponse] mustBe emptyDataItem
    }


    "return 500 when stub simulates failure" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/ct-form-data/999/1?startDate=2006-01-01&endDate=2006-12-31").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/accounting-period-details/2/1").futureValue
      response.status mustBe UNAUTHORIZED
    }

  }


}

