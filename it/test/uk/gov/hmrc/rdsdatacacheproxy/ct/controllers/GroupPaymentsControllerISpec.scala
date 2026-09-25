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
import play.api.http.Status.*
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.JSON
import uk.gov.hmrc.rdsdatacacheproxy.ct.helpers.GroupPaymentsHelper
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.{GpaPaymentsDetails, GroupReferenceNumberLstItem, GroupSummaryDetails}
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.GroupPaymentsRepository
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}


class GroupPaymentsControllerISpec extends AnyWordSpec
  with Matchers with ScalaFutures
  with IntegrationPatience with ApplicationWithWiremock with GroupPaymentsHelper {

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[GroupPaymentsRepository].toInstance(new GroupPaymentsRepositoryDataSource)
      )
      .build()


  "GET /corporation-tax/group-summary" should {
    val endpoint = "/corporation-tax/group-summary"

    "return 200 with correct Group Payment" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/1/1").futureValue

      response.status mustBe OK
      response.contentType mustBe JSON
      response.json.as[GroupSummaryDetails] mustBe GroupSummaryDetails(
        gpaGrpSummaryDetails = List(
          groupSummaryDetItemOne
        ),
        gpaReferenceNumberLst = List(
          GroupReferenceNumberLstItem(112)
        ),
        nominatedCompanyName = "Some company name"
      )
    }

    "return 200 with Group Payment :: empty refs and details" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/3/2").futureValue

      response.status mustBe OK
      response.contentType mustBe JSON
      response.json.as[GroupSummaryDetails] mustBe GroupSummaryDetails(
        gpaGrpSummaryDetails = List.empty,
        gpaReferenceNumberLst = List.empty,
        nominatedCompanyName = "Empty company name"
      )
    }

    "return 404 when no data found" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/5/2").futureValue

      response.status mustBe NOT_FOUND
      response.contentType mustBe JSON

    }

    "return 500 with when a downstream error occurs" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/99/1").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/99/1").futureValue
      response.status mustBe UNAUTHORIZED
    }

  }

  "GET /corporation-tax/gpa-payment-details" should {
    val endpoint = "/corporation-tax/gpa-payment-details"

    "return 200 with correct Get Payments Details: default record" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/1?contractVersion=1&startIndex=1&count=1").futureValue

      response.status mustBe OK
      response.contentType mustBe JSON
      response.json.as[GpaPaymentsDetails] mustBe defaultPaymentDetails
    }


    "return 200 with correct Get Payments Details: empty record" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/11?contractVersion=1&startIndex=1&count=1").futureValue

      response.status mustBe OK
      response.contentType mustBe JSON
      response.json.as[GpaPaymentsDetails] mustBe emptyPaymentDetailsRec
    }

    "return 404 when no data found" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/99?contractVersion=1&startIndex=1&count=1").futureValue

      response.status mustBe NOT_FOUND
      response.contentType mustBe JSON

    }

    "return 500 with when a downstream error occurs" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/99?contractVersion=1&startIndex=1&count=1"").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR
    }

    "return 401 when unauthorised" in {
      AuthStub.unauthorised()
      val response = get(s"$endpoint/99/1").futureValue
      response.status mustBe UNAUTHORIZED
    }

  }

}
