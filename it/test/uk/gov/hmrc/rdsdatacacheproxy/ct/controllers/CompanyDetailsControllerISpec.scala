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
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK, UNAUTHORIZED}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, inject}
import uk.gov.hmrc.rdsdatacacheproxy.ct.models.CompanyDetailsResponse
import uk.gov.hmrc.rdsdatacacheproxy.ct.repositories.CompanyDetailsRepository
import uk.gov.hmrc.rdsdatacacheproxy.ct.stub.CompanyDetailsStubData
import uk.gov.hmrc.rdsdatacacheproxy.itutil.{ApplicationWithWiremock, AuthStub}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompanyDetailsControllerISpec extends AnyWordSpec with Matchers with ScalaFutures with IntegrationPatience with ApplicationWithWiremock {

  class CompanyDetailsStub extends CompanyDetailsRepository {
    override def getCompanyDetails(taxPayerReference: Long): Future[CompanyDetailsResponse] =
      Future {
        CompanyDetailsStubData.getCompanyDetails(taxPayerReference)
      }
  }

  override lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(extraConfig)
      .overrides(
        bind[CompanyDetailsRepository].toInstance(new CompanyDetailsStub)
      )
      .build()


   private final val endpoint = "/corporation-tax"


  "GET /corporation-tax/company-details" should {

    "return 200 with CompanyDetailsResponse list contains multiple items" in {
      AuthStub.authorised()

      val response = get(s"$endpoint/company-details/20").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[CompanyDetailsResponse] mustBe CompanyDetailsStubData.multipleCompaniesResponse
    }

    "return 200 with CompanyDetailsResponse single list" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/company-details/10").futureValue

      response.status mustBe OK
      response.contentType mustBe "application/json"

      response.json.as[CompanyDetailsResponse] mustBe CompanyDetailsStubData.singleCompanyResponse

    }

    "return 500 when stub simulates failure" in {

      AuthStub.authorised()

      val response = get(s"$endpoint/company-details/200").futureValue

      response.status mustBe INTERNAL_SERVER_ERROR

    }

    "return 401 when unauthorised" in {

      AuthStub.unauthorised()

      val response = get(s"$endpoint/company-details/3").futureValue

      response.status mustBe UNAUTHORIZED
    }
  }


}
